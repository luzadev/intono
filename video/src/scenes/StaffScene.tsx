import {
  AbsoluteFill,
  interpolate,
  spring,
  useCurrentFrame,
  useVideoConfig,
} from "remotion";
import { COLORS, NOTES_ITALIAN } from "../lib/constants";

const STAFF_TOP = 300;
const STAFF_GAP = 40;
const STAFF_LEFT = 200;
const NOTE_START_X = 380;
const NOTE_GAP = 150;

const StaffLine: React.FC<{ y: number; delay: number }> = ({ y, delay }) => {
  const frame = useCurrentFrame();
  const localFrame = frame - delay;

  const width = interpolate(localFrame, [0, 30], [0, 1520], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  return (
    <div
      style={{
        position: "absolute",
        left: STAFF_LEFT,
        top: y,
        width,
        height: 2,
        background: COLORS.staffLine,
        borderRadius: 1,
      }}
    />
  );
};

const NoteHead: React.FC<{
  x: number;
  lineOffset: number;
  name: string;
  index: number;
  isPlaying: boolean;
  isHigh: boolean;
}> = ({ x, lineOffset, name, index, isPlaying, isHigh }) => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const entryDelay = 50 + index * 18;

  const noteSpring = spring({
    frame: Math.max(0, frame - entryDelay),
    fps,
    config: { damping: 10, stiffness: 100, mass: 0.6 },
  });

  const y = STAFF_TOP + lineOffset * STAFF_GAP - 18;
  const opacity = interpolate(noteSpring, [0, 1], [0, 1]);
  const scale = interpolate(noteSpring, [0, 1], [0, 1]);
  const noteY = interpolate(noteSpring, [0, 1], [-40, 0]);

  const glowIntensity = isPlaying
    ? interpolate(
        Math.sin(frame * 0.3),
        [-1, 1],
        [0.6, 1],
      )
    : 0;

  // Stem direction based on position
  const stemUp = lineOffset > 2;

  // Ledger line for notes outside the staff
  const needsLedgerLine = lineOffset >= 6;

  return (
    <div
      style={{
        position: "absolute",
        left: x - 18,
        top: y + noteY,
        opacity,
        transform: `scale(${scale})`,
      }}
    >
      {/* Ledger line */}
      {needsLedgerLine && (
        <div
          style={{
            position: "absolute",
            left: -8,
            top: 17,
            width: 52,
            height: 2,
            background: COLORS.staffLine,
          }}
        />
      )}

      {/* Note glow */}
      {isPlaying && (
        <div
          style={{
            position: "absolute",
            left: -10,
            top: -10,
            width: 56,
            height: 56,
            borderRadius: "50%",
            background: `radial-gradient(circle, ${COLORS.noteGlow}, transparent)`,
            opacity: glowIntensity,
          }}
        />
      )}

      {/* Note head (ellipse) */}
      <div
        style={{
          width: 36,
          height: 28,
          borderRadius: "50%",
          background: isPlaying ? COLORS.accent : COLORS.text,
          transform: "rotate(-15deg)",
          boxShadow: isPlaying
            ? `0 0 20px ${COLORS.noteGlow}, 0 0 40px ${COLORS.noteGlow}`
            : "none",
        }}
      />

      {/* Stem */}
      <div
        style={{
          position: "absolute",
          left: stemUp ? 33 : 0,
          top: stemUp ? -60 : 18,
          width: 3,
          height: 65,
          background: isPlaying ? COLORS.accent : COLORS.text,
          borderRadius: 1.5,
        }}
      />

      {/* Note name label */}
      <div
        style={{
          position: "absolute",
          left: "50%",
          top: stemUp ? 45 : -35,
          transform: "translateX(-50%)",
          fontSize: 22,
          fontWeight: 700,
          color: isPlaying ? COLORS.accent : COLORS.textMuted,
          fontFamily:
            'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
          textAlign: "center",
          letterSpacing: 1,
          whiteSpace: "nowrap",
          textShadow: isPlaying
            ? `0 0 10px ${COLORS.noteGlow}`
            : "none",
        }}
      >
        {name}
        {isHigh && (
          <span style={{ fontSize: 14, verticalAlign: "super" }}>2</span>
        )}
      </div>
    </div>
  );
};

const TrebleClef: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const clefSpring = spring({
    frame: Math.max(0, frame - 25),
    fps,
    config: { damping: 12, stiffness: 60, mass: 1 },
  });

  const opacity = interpolate(clefSpring, [0, 1], [0, 0.9]);
  const scale = interpolate(clefSpring, [0, 1], [0.3, 1]);

  return (
    <div
      style={{
        position: "absolute",
        left: STAFF_LEFT + 20,
        top: STAFF_TOP - 65,
        fontSize: 200,
        color: COLORS.accent,
        opacity,
        transform: `scale(${scale})`,
        lineHeight: 1,
        textShadow: `0 0 30px ${COLORS.noteGlow}`,
      }}
    >
      {"\uD834\uDD1E"}
    </div>
  );
};

const Playhead: React.FC = () => {
  const frame = useCurrentFrame();

  const playStart = 70;
  const playEnd = 210;

  const progress = interpolate(frame, [playStart, playEnd], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  const x = interpolate(progress, [0, 1], [NOTE_START_X - 30, NOTE_START_X + NOTE_GAP * 7 + 30]);

  const opacity = interpolate(frame, [playStart - 10, playStart, playEnd, playEnd + 10], [0, 0.8, 0.8, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  return (
    <div
      style={{
        position: "absolute",
        left: x,
        top: STAFF_TOP - 30,
        width: 3,
        height: STAFF_GAP * 4 + 60,
        background: `linear-gradient(180deg, transparent, ${COLORS.accent}, transparent)`,
        opacity,
        boxShadow: `0 0 15px ${COLORS.accent}, 0 0 30px ${COLORS.noteGlow}`,
      }}
    />
  );
};

export const StaffScene: React.FC = () => {
  const frame = useCurrentFrame();

  // Scene fade
  const fadeIn = interpolate(frame, [0, 30], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const fadeOut = interpolate(frame, [240, 270], [1, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // Section title
  const titleOpacity = interpolate(frame, [10, 30], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const titleY = interpolate(frame, [10, 30], [20, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // Calculate which note is "playing"
  const playStart = 70;
  const noteDuration = 18;
  const activeNoteIndex = Math.floor(
    (frame - playStart) / noteDuration
  );

  // Bottom scale visualization
  const scaleBarCount = 24;

  return (
    <AbsoluteFill style={{ opacity: fadeIn * fadeOut }}>
      {/* Section title */}
      <div
        style={{
          position: "absolute",
          left: "50%",
          top: 140,
          transform: `translate(-50%, ${titleY}px)`,
          opacity: titleOpacity,
          fontSize: 48,
          fontWeight: 700,
          color: COLORS.text,
          letterSpacing: 2,
          fontFamily:
            'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
        }}
      >
        La Scala di{" "}
        <span style={{ color: COLORS.accent }}>DO Maggiore</span>
      </div>

      {/* Staff lines */}
      {Array.from({ length: 5 }, (_, i) => (
        <StaffLine
          key={i}
          y={STAFF_TOP + i * STAFF_GAP}
          delay={5 + i * 4}
        />
      ))}

      {/* Treble clef */}
      <TrebleClef />

      {/* Playhead */}
      <Playhead />

      {/* Notes */}
      {NOTES_ITALIAN.map((note, i) => (
        <NoteHead
          key={i}
          x={NOTE_START_X + i * NOTE_GAP}
          lineOffset={note.offset}
          name={note.name}
          index={i}
          isPlaying={activeNoteIndex === i && frame >= playStart}
          isHigh={i === 7}
        />
      ))}

      {/* Bottom frequency bars */}
      <div
        style={{
          position: "absolute",
          bottom: 100,
          left: "50%",
          transform: "translateX(-50%)",
          display: "flex",
          gap: 6,
          alignItems: "flex-end",
          height: 80,
        }}
      >
        {Array.from({ length: scaleBarCount }, (_, i) => {
          const barHeight = interpolate(
            Math.sin(frame * 0.08 + i * 0.5),
            [-1, 1],
            [10, 60 + (activeNoteIndex >= 0 && activeNoteIndex < 8 ? 20 : 0)],
          );
          const barOpacity = interpolate(frame, [60, 80], [0, 0.5], {
            extrapolateLeft: "clamp",
            extrapolateRight: "clamp",
          });

          return (
            <div
              key={i}
              style={{
                width: 8,
                height: barHeight,
                borderRadius: 4,
                background: `linear-gradient(180deg, ${COLORS.accent}, ${COLORS.accentWarm})`,
                opacity: barOpacity,
              }}
            />
          );
        })}
      </div>

      {/* Brace decoration */}
      <div
        style={{
          position: "absolute",
          left: STAFF_LEFT - 30,
          top: STAFF_TOP - 10,
          width: 3,
          height: STAFF_GAP * 4 + 20,
          background: COLORS.accent,
          borderRadius: 2,
          opacity: interpolate(frame, [20, 40], [0, 0.6], {
            extrapolateLeft: "clamp",
            extrapolateRight: "clamp",
          }),
        }}
      />
    </AbsoluteFill>
  );
};
