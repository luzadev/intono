import { writeFileSync } from "fs";

const SAMPLE_RATE = 44100;
const DURATION = 30; // seconds
const TOTAL_SAMPLES = SAMPLE_RATE * DURATION;

// Note frequencies (Hz)
const FREQ = {
  C3: 130.81, E3: 164.81, G3: 196.0,
  C4: 261.63, D4: 293.66, E4: 329.63, F4: 349.23,
  G4: 392.0, A4: 440.0, B4: 493.88, C5: 523.25,
  C2: 65.41, G2: 98.0,
};

// C major scale for the staff scene
const SCALE = [FREQ.C4, FREQ.D4, FREQ.E4, FREQ.F4, FREQ.G4, FREQ.A4, FREQ.B4, FREQ.C5];

function envelope(t, attack, sustain, release, totalDur) {
  if (t < attack) return t / attack;
  if (t < attack + sustain) return 1.0;
  const rel = t - attack - sustain;
  if (rel < release) return 1.0 - rel / release;
  return 0;
}

function softSine(t, freq) {
  // Sine with soft harmonics for a piano-like tone
  return (
    Math.sin(2 * Math.PI * freq * t) * 0.6 +
    Math.sin(2 * Math.PI * freq * 2 * t) * 0.2 +
    Math.sin(2 * Math.PI * freq * 3 * t) * 0.1 +
    Math.sin(2 * Math.PI * freq * 4 * t) * 0.05
  );
}

function padTone(t, freq) {
  // Very soft, warm pad sound
  return (
    Math.sin(2 * Math.PI * freq * t) * 0.5 +
    Math.sin(2 * Math.PI * freq * 1.001 * t) * 0.3 + // slight detune for warmth
    Math.sin(2 * Math.PI * freq * 2 * t) * 0.1
  );
}

// Simple reverb via comb filter
function applyReverb(samples, delay, decay) {
  const delaySamples = Math.floor(delay * SAMPLE_RATE);
  const out = new Float64Array(samples.length);
  for (let i = 0; i < samples.length; i++) {
    out[i] = samples[i];
    if (i >= delaySamples) {
      out[i] += out[i - delaySamples] * decay;
    }
  }
  return out;
}

// Generate samples
const left = new Float64Array(TOTAL_SAMPLES);
const right = new Float64Array(TOTAL_SAMPLES);

for (let i = 0; i < TOTAL_SAMPLES; i++) {
  const t = i / SAMPLE_RATE; // time in seconds
  let sampleL = 0;
  let sampleR = 0;

  // === 1. Ambient pad (entire duration, fades in/out) ===
  const padEnv =
    envelope(t, 3.0, 22.0, 5.0, DURATION) * 0.12;

  // C major chord pad (C3, E3, G3)
  const pad =
    padTone(t, FREQ.C3) * 0.4 +
    padTone(t, FREQ.E3) * 0.3 +
    padTone(t, FREQ.G3) * 0.3;

  sampleL += pad * padEnv;
  sampleR += pad * padEnv;

  // Sub bass
  const bassEnv = envelope(t, 4.0, 20.0, 6.0, DURATION) * 0.08;
  sampleL += Math.sin(2 * Math.PI * FREQ.C2 * t) * bassEnv;
  sampleR += Math.sin(2 * Math.PI * FREQ.C2 * t) * bassEnv;

  // === 2. Intro shimmer (0-6s) ===
  if (t < 7) {
    const shimmerEnv = envelope(t, 1.0, 3.5, 2.5, 7.0) * 0.06;
    const shimmer =
      Math.sin(2 * Math.PI * FREQ.G4 * t) * 0.5 +
      Math.sin(2 * Math.PI * FREQ.C5 * t) * 0.3 +
      Math.sin(2 * Math.PI * FREQ.E4 * t) * 0.2;
    // Slight stereo spread
    sampleL += shimmer * shimmerEnv * 1.1;
    sampleR += shimmer * shimmerEnv * 0.9;
  }

  // === 3. Scale notes during staff scene (6-15s) ===
  const scaleStart = 6.0; // seconds
  const noteDuration = 1.0; // each note lasts 1s
  for (let n = 0; n < SCALE.length; n++) {
    const noteStart = scaleStart + n * noteDuration;
    const noteT = t - noteStart;
    if (noteT >= 0 && noteT < 2.0) {
      // Note with decay
      const noteEnv = envelope(noteT, 0.02, 0.3, 1.5, 2.0) * 0.22;
      const note = softSine(t, SCALE[n]);
      // Add fifth harmonic quietly
      const fifth = softSine(t, SCALE[n] * 1.5) * 0.08;
      sampleL += (note + fifth) * noteEnv;
      sampleR += (note + fifth) * noteEnv;
    }
  }

  // === 4. Feature section arpeggios (14-24s) ===
  if (t >= 14 && t < 24) {
    const arpNotes = [FREQ.C4, FREQ.E4, FREQ.G4, FREQ.C5];
    const arpSpeed = 0.6; // seconds per note
    const arpT = t - 14;
    const arpIndex = Math.floor(arpT / arpSpeed) % arpNotes.length;
    const noteT = arpT % arpSpeed;
    const arpEnv = envelope(noteT, 0.01, 0.15, 0.4, arpSpeed) * 0.1;
    const fadeInOut =
      envelope(t - 14, 1.5, 6.0, 2.5, 10.0);
    const arp = softSine(t, arpNotes[arpIndex]);
    // Alternate stereo slightly
    sampleL += arp * arpEnv * fadeInOut * (arpIndex % 2 === 0 ? 1.1 : 0.9);
    sampleR += arp * arpEnv * fadeInOut * (arpIndex % 2 === 0 ? 0.9 : 1.1);
  }

  // === 5. Outro resolution chord (23-30s) ===
  if (t >= 23) {
    const outroT = t - 23;
    const chordEnv = envelope(outroT, 2.0, 2.5, 2.5, 7.0) * 0.15;
    // Rich C major chord
    const chord =
      padTone(t, FREQ.C3) * 0.25 +
      padTone(t, FREQ.E3) * 0.2 +
      padTone(t, FREQ.G3) * 0.2 +
      padTone(t, FREQ.C4) * 0.2 +
      padTone(t, FREQ.E4) * 0.1 +
      padTone(t, FREQ.G4) * 0.05;
    sampleL += chord * chordEnv;
    sampleR += chord * chordEnv;

    // High sparkle at the end
    if (outroT > 1.0 && outroT < 4.0) {
      const sparkleEnv = envelope(outroT - 1.0, 0.5, 1.0, 1.5, 3.0) * 0.04;
      sampleL += Math.sin(2 * Math.PI * FREQ.C5 * 2 * t) * sparkleEnv;
      sampleR += Math.sin(2 * Math.PI * FREQ.G4 * 2 * t) * sparkleEnv;
    }
  }

  left[i] = sampleL;
  right[i] = sampleR;
}

// Apply simple reverb
const reverbL = applyReverb(left, 0.08, 0.3);
const reverbR = applyReverb(right, 0.11, 0.28);

// Mix dry + wet
for (let i = 0; i < TOTAL_SAMPLES; i++) {
  left[i] = left[i] * 0.65 + reverbL[i] * 0.35;
  right[i] = right[i] * 0.65 + reverbR[i] * 0.35;
}

// Normalize
let maxAmp = 0;
for (let i = 0; i < TOTAL_SAMPLES; i++) {
  maxAmp = Math.max(maxAmp, Math.abs(left[i]), Math.abs(right[i]));
}
const normFactor = maxAmp > 0 ? 0.85 / maxAmp : 1;

// Convert to 16-bit PCM WAV
const numChannels = 2;
const bitsPerSample = 16;
const byteRate = SAMPLE_RATE * numChannels * (bitsPerSample / 8);
const blockAlign = numChannels * (bitsPerSample / 8);
const dataSize = TOTAL_SAMPLES * numChannels * (bitsPerSample / 8);
const fileSize = 44 + dataSize;

const buffer = Buffer.alloc(fileSize);
let offset = 0;

// RIFF header
buffer.write("RIFF", offset); offset += 4;
buffer.writeUInt32LE(fileSize - 8, offset); offset += 4;
buffer.write("WAVE", offset); offset += 4;

// fmt chunk
buffer.write("fmt ", offset); offset += 4;
buffer.writeUInt32LE(16, offset); offset += 4;
buffer.writeUInt16LE(1, offset); offset += 2; // PCM
buffer.writeUInt16LE(numChannels, offset); offset += 2;
buffer.writeUInt32LE(SAMPLE_RATE, offset); offset += 4;
buffer.writeUInt32LE(byteRate, offset); offset += 4;
buffer.writeUInt16LE(blockAlign, offset); offset += 2;
buffer.writeUInt16LE(bitsPerSample, offset); offset += 2;

// data chunk
buffer.write("data", offset); offset += 4;
buffer.writeUInt32LE(dataSize, offset); offset += 4;

// Interleaved stereo samples
for (let i = 0; i < TOTAL_SAMPLES; i++) {
  const l = Math.max(-1, Math.min(1, left[i] * normFactor));
  const r = Math.max(-1, Math.min(1, right[i] * normFactor));
  buffer.writeInt16LE(Math.round(l * 32767), offset); offset += 2;
  buffer.writeInt16LE(Math.round(r * 32767), offset); offset += 2;
}

writeFileSync("public/music.wav", buffer);
console.log(`Generated music.wav (${(fileSize / 1024 / 1024).toFixed(1)} MB, ${DURATION}s stereo)`);
