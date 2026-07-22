import { AbsoluteFill, interpolate, random, spring, useCurrentFrame, useVideoConfig } from "remotion";
import { COLORS, MUSIC_SYMBOLS } from "../lib/constants";

const FloatingNote: React.FC<{
  symbol: string;
  startX: number;
  startY: number;
  size: number;
  delay: number;
  speed: number;
  drift: number;
}> = ({ symbol, startX, startY, size, delay, speed, drift }) => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const progress = interpolate(frame - delay, [0, 180 / speed], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  const y = interpolate(progress, [0, 1], [startY, startY - 600]);
  const x = startX + Math.sin(progress * Math.PI * 2 * drift) * 40;
  const opacity = interpolate(
    progress,
    [0, 0.1, 0.7, 1],
    [0, 0.6, 0.6, 0],
  );
  const rotation = interpolate(progress, [0, 1], [0, drift * 30]);

  const scale = spring({
    frame: Math.max(0, frame - delay),
    fps,
    config: { damping: 12, stiffness: 80, mass: 0.8 },
  });

  return (
    <div
      style={{
        position: "absolute",
        left: x,
        top: y,
        fontSize: size,
        opacity,
        transform: `scale(${scale}) rotate(${rotation}deg)`,
        color: COLORS.accent,
        textShadow: `0 0 20px ${COLORS.noteGlow}, 0 0 40px ${COLORS.noteGlow}`,
        pointerEvents: "none",
      }}
    >
      {symbol}
    </div>
  );
};

const Particle: React.FC<{
  x: number;
  y: number;
  delay: number;
  size: number;
}> = ({ x, y, delay, size }) => {
  const frame = useCurrentFrame();

  const progress = interpolate(frame - delay, [0, 120], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  const opacity = interpolate(progress, [0, 0.3, 0.7, 1], [0, 0.8, 0.8, 0]);
  const scale = interpolate(progress, [0, 0.5, 1], [0.5, 1.2, 0.5]);

  return (
    <div
      style={{
        position: "absolute",
        left: x,
        top: y + Math.sin(frame * 0.05 + delay) * 10,
        width: size,
        height: size,
        borderRadius: "50%",
        background: `radial-gradient(circle, ${COLORS.accent}, transparent)`,
        opacity,
        transform: `scale(${scale})`,
      }}
    />
  );
};

export const IntroScene: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps, width, height } = useVideoConfig();

  // Title animation
  const titleSpring = spring({
    frame: Math.max(0, frame - 40),
    fps,
    config: { damping: 14, stiffness: 60, mass: 1 },
  });

  const titleOpacity = interpolate(titleSpring, [0, 1], [0, 1]);
  const titleScale = interpolate(titleSpring, [0, 1], [0.3, 1]);
  const titleY = interpolate(titleSpring, [0, 1], [60, 0]);

  // Subtitle animation
  const subtitleSpring = spring({
    frame: Math.max(0, frame - 70),
    fps,
    config: { damping: 14, stiffness: 50, mass: 1 },
  });

  const subtitleOpacity = interpolate(subtitleSpring, [0, 1], [0, 1]);
  const subtitleY = interpolate(subtitleSpring, [0, 1], [30, 0]);

  // Treble clef animation
  const clefSpring = spring({
    frame: Math.max(0, frame - 20),
    fps,
    config: { damping: 18, stiffness: 40, mass: 1.2 },
  });

  const clefOpacity = interpolate(clefSpring, [0, 1], [0, 0.15]);
  const clefScale = interpolate(clefSpring, [0, 1], [0.5, 1]);
  const clefRotation = interpolate(clefSpring, [0, 1], [-15, 0]);

  // Scene fade out
  const fadeOut = interpolate(frame, [170, 210], [1, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // Generate floating notes
  const floatingNotes = Array.from({ length: 18 }, (_, i) => ({
    symbol: MUSIC_SYMBOLS[i % MUSIC_SYMBOLS.length],
    startX: (width / 18) * i + random(`note-${i}`) * 80 - 40,
    startY: height + 50 + (i % 3) * 80,
    size: 28 + (i % 4) * 12,
    delay: i * 8,
    speed: 0.8 + (i % 3) * 0.3,
    drift: 0.5 + (i % 4) * 0.4,
  }));

  // Generate particles
  const particles = Array.from({ length: 30 }, (_, i) => ({
    x: Math.sin(i * 2.39) * width * 0.45 + width / 2,
    y: Math.cos(i * 1.73) * height * 0.4 + height / 2,
    delay: i * 4,
    size: 2 + (i % 4) * 2,
  }));

  // Pulsing ring
  const ringProgress = interpolate(frame, [60, 180], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const ringScale = interpolate(ringProgress, [0, 1], [0.5, 2.5]);
  const ringOpacity = interpolate(ringProgress, [0, 0.3, 1], [0, 0.3, 0]);

  return (
    <AbsoluteFill style={{ opacity: fadeOut }}>
      {/* Large treble clef background */}
      <div
        style={{
          position: "absolute",
          right: width * 0.15,
          top: "50%",
          transform: `translateY(-50%) scale(${clefScale}) rotate(${clefRotation}deg)`,
          fontSize: 500,
          opacity: clefOpacity,
          color: COLORS.accent,
          lineHeight: 1,
        }}
      >
        {"\uD834\uDD1E"}
      </div>

      {/* Pulsing ring */}
      <div
        style={{
          position: "absolute",
          left: "50%",
          top: "50%",
          width: 300,
          height: 300,
          marginLeft: -150,
          marginTop: -150,
          borderRadius: "50%",
          border: `2px solid ${COLORS.accent}`,
          opacity: ringOpacity,
          transform: `scale(${ringScale})`,
        }}
      />

      {/* Floating notes */}
      {floatingNotes.map((note, i) => (
        <FloatingNote key={i} {...note} />
      ))}

      {/* Particles */}
      {particles.map((p, i) => (
        <Particle key={`p-${i}`} {...p} />
      ))}

      {/* Title */}
      <div
        style={{
          position: "absolute",
          left: "50%",
          top: "42%",
          transform: `translate(-50%, -50%) scale(${titleScale}) translateY(${titleY}px)`,
          opacity: titleOpacity,
          textAlign: "center",
        }}
      >
        <div
          style={{
            fontSize: 140,
            fontWeight: 800,
            color: COLORS.text,
            letterSpacing: -3,
            textShadow: `0 0 60px ${COLORS.noteGlow}, 0 0 120px rgba(255,215,0,0.2)`,
            fontFamily:
              'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
          }}
        >
          In
          <span style={{ color: COLORS.accent }}>Tono</span>
        </div>
      </div>

      {/* Subtitle */}
      <div
        style={{
          position: "absolute",
          left: "50%",
          top: "56%",
          transform: `translate(-50%, -50%) translateY(${subtitleY}px)`,
          opacity: subtitleOpacity,
          textAlign: "center",
        }}
      >
        <div
          style={{
            fontSize: 36,
            fontWeight: 300,
            color: COLORS.textMuted,
            letterSpacing: 8,
            textTransform: "uppercase",
            fontFamily:
              'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
          }}
        >
          La tua guida musicale
        </div>
      </div>

      {/* Bottom decorative line */}
      <div
        style={{
          position: "absolute",
          bottom: 120,
          left: "50%",
          transform: "translateX(-50%)",
        }}
      >
        <div
          style={{
            width: interpolate(frame, [80, 130], [0, 300], {
              extrapolateLeft: "clamp",
              extrapolateRight: "clamp",
            }),
            height: 2,
            background: `linear-gradient(90deg, transparent, ${COLORS.accent}, transparent)`,
          }}
        />
      </div>
    </AbsoluteFill>
  );
};
