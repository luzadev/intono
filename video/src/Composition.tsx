import {
  AbsoluteFill,
  Audio,
  interpolate,
  Sequence,
  staticFile,
  useCurrentFrame,
} from "remotion";
import { COLORS, SCENE_TIMING } from "./lib/constants";
import { IntroScene } from "./scenes/IntroScene";
import { StaffScene } from "./scenes/StaffScene";
import { FeaturesScene } from "./scenes/FeaturesScene";
import { OutroScene } from "./scenes/OutroScene";

const AnimatedBackground: React.FC = () => {
  const frame = useCurrentFrame();

  // Slow gradient rotation
  const angle = interpolate(frame, [0, 900], [0, 60]);

  // Subtle purple nebula blobs
  const blob1X = 30 + Math.sin(frame * 0.008) * 10;
  const blob1Y = 30 + Math.cos(frame * 0.006) * 8;
  const blob2X = 70 + Math.sin(frame * 0.01 + 2) * 12;
  const blob2Y = 60 + Math.cos(frame * 0.007 + 1) * 10;

  return (
    <AbsoluteFill>
      {/* Base gradient */}
      <div
        style={{
          position: "absolute",
          inset: 0,
          background: `linear-gradient(${angle}deg, ${COLORS.bgDark} 0%, ${COLORS.bgPurple} 40%, ${COLORS.bgIndigo} 70%, ${COLORS.bgDark} 100%)`,
        }}
      />

      {/* Nebula blob 1 */}
      <div
        style={{
          position: "absolute",
          left: `${blob1X}%`,
          top: `${blob1Y}%`,
          width: 600,
          height: 600,
          borderRadius: "50%",
          background: `radial-gradient(circle, rgba(106,55,185,0.15), transparent 70%)`,
          filter: "blur(80px)",
        }}
      />

      {/* Nebula blob 2 */}
      <div
        style={{
          position: "absolute",
          left: `${blob2X}%`,
          top: `${blob2Y}%`,
          width: 500,
          height: 500,
          borderRadius: "50%",
          background: `radial-gradient(circle, rgba(255,215,0,0.06), transparent 70%)`,
          filter: "blur(60px)",
        }}
      />

      {/* Subtle vignette */}
      <div
        style={{
          position: "absolute",
          inset: 0,
          background:
            "radial-gradient(ellipse at center, transparent 40%, rgba(0,0,0,0.5) 100%)",
        }}
      />

      {/* Subtle grain overlay */}
      <svg
        style={{
          position: "absolute",
          inset: 0,
          width: "100%",
          height: "100%",
          opacity: 0.03,
        }}
      >
        <filter id="grain">
          <feTurbulence
            type="fractalNoise"
            baseFrequency="0.9"
            numOctaves="4"
            stitchTiles="stitch"
          />
        </filter>
        <rect width="100%" height="100%" filter="url(#grain)" />
      </svg>
    </AbsoluteFill>
  );
};

export const MyComposition: React.FC = () => {
  return (
    <AbsoluteFill style={{ backgroundColor: COLORS.bgDark }}>
      {/* Background music */}
      <Audio src={staticFile("music.wav")} volume={0.8} />

      {/* Animated background - always present */}
      <AnimatedBackground />

      {/* Scene 1: Intro (frames 0-210) */}
      <Sequence from={SCENE_TIMING.intro.start} durationInFrames={SCENE_TIMING.intro.end}>
        <IntroScene />
      </Sequence>

      {/* Scene 2: Musical Staff (frames 180-450) */}
      <Sequence from={SCENE_TIMING.staff.start} durationInFrames={SCENE_TIMING.staff.end - SCENE_TIMING.staff.start}>
        <StaffScene />
      </Sequence>

      {/* Scene 3: Features Showcase (frames 420-720) */}
      <Sequence from={SCENE_TIMING.features.start} durationInFrames={SCENE_TIMING.features.end - SCENE_TIMING.features.start}>
        <FeaturesScene />
      </Sequence>

      {/* Scene 4: Outro (frames 690-900) */}
      <Sequence from={SCENE_TIMING.outro.start} durationInFrames={SCENE_TIMING.outro.end - SCENE_TIMING.outro.start}>
        <OutroScene />
      </Sequence>
    </AbsoluteFill>
  );
};
