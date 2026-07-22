export const COLORS = {
  bgDark: "#0a0118",
  bgPurple: "#1a0533",
  bgIndigo: "#2d1b69",
  accent: "#FFD700",
  accentWarm: "#FFA500",
  accentRose: "#FF6B9D",
  text: "#FFFFFF",
  textMuted: "rgba(255,255,255,0.6)",
  staffLine: "rgba(255,255,255,0.35)",
  cardBg: "rgba(255,255,255,0.06)",
  cardBorder: "rgba(255,255,255,0.12)",
  noteGlow: "rgba(255,215,0,0.4)",
};

export const NOTES_ITALIAN = [
  { name: "DO", offset: 6 },
  { name: "RE", offset: 5 },
  { name: "MI", offset: 4 },
  { name: "FA", offset: 3.5 },
  { name: "SOL", offset: 2.5 },
  { name: "LA", offset: 1.5 },
  { name: "SI", offset: 1 },
  { name: "DO", offset: 0.5 },
] as const;

export const MUSIC_SYMBOLS = [
  "\u266A",
  "\u266B",
  "\u266C",
  "\u2669",
  "\u{1D160}",
  "\u{1D15E}",
];

export const FEATURES = [
  {
    icon: "\uD83C\uDFAF",
    title: "Accordatore",
    desc: "Rileva l'intonazione in tempo reale",
    color: "#4ECDC4",
  },
  {
    icon: "\uD83C\uDFB5",
    title: "Pratica Guidata",
    desc: "Segui le sequenze di note passo dopo passo",
    color: "#FFD93D",
  },
  {
    icon: "\uD83D\uDCC4",
    title: "Spartiti",
    desc: "Libreria con oltre 190 brani classici",
    color: "#6C5CE7",
  },
  {
    icon: "\uD83D\uDCF8",
    title: "Scansione AI",
    desc: "Riconosci spartiti con intelligenza artificiale",
    color: "#FF6B9D",
  },
] as const;

export const INSTRUMENTS = [
  { name: "Violino", emoji: "\uD83C\uDFBB", range: "G3-E7" },
  { name: "Viola", emoji: "\uD83C\uDFBB", range: "C3-A6" },
  { name: "Violoncello", emoji: "\uD83C\uDFBB", range: "C2-C6" },
  { name: "Pianoforte", emoji: "\uD83C\uDFB9", range: "A0-C8" },
  { name: "Chitarra", emoji: "\uD83C\uDFB8", range: "E2-E6" },
  { name: "Voce", emoji: "\uD83C\uDFA4", range: "D2-D6" },
] as const;

export const SCENE_TIMING = {
  intro: { start: 0, end: 210 },
  staff: { start: 180, end: 450 },
  features: { start: 420, end: 720 },
  outro: { start: 690, end: 900 },
} as const;

export const VIDEO_CONFIG = {
  durationInFrames: 900,
  fps: 30,
  width: 1920,
  height: 1080,
} as const;
