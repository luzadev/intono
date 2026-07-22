import {
  AbsoluteFill,
  interpolate,
  spring,
  useCurrentFrame,
  useVideoConfig,
} from "remotion";
import { COLORS, MUSIC_SYMBOLS } from "../lib/constants";

const OrbitNote: React.FC<{
  index: number;
  total: number;
  radius: number;
  speed: number;
  size: number;
}> = ({ index, total, radius, speed, size }) => {
  const frame = useCurrentFrame();
  const { width, height } = useVideoConfig();

  const angle =
    (index / total) * Math.PI * 2 + frame * speed * 0.02;

  const x = width / 2 + Math.cos(angle) * radius - size / 2;
  const y = height / 2 + Math.sin(angle) * radius * 0.6 - size / 2 - 40;

  const orbitOpacity = interpolate(frame, [30, 60, 170, 210], [0, 0.5, 0.5, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  return (
    <div
      style={{
        position: "absolute",
        left: x,
        top: y,
        fontSize: size,
        color: COLORS.accent,
        opacity: orbitOpacity,
        textShadow: `0 0 15px ${COLORS.noteGlow}`,
      }}
    >
      {MUSIC_SYMBOLS[index % MUSIC_SYMBOLS.length]}
    </div>
  );
};

const ConcentricRing: React.FC<{
  radius: number;
  delay: number;
}> = ({ radius, delay }) => {
  const frame = useCurrentFrame();

  const progress = interpolate(frame - delay, [0, 60], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  const scale = interpolate(progress, [0, 1], [0.5, 1]);
  const opacity = interpolate(
    progress,
    [0, 0.3, 0.7, 1],
    [0, 0.2, 0.2, 0.08],
  );

  return (
    <div
      style={{
        position: "absolute",
        left: "50%",
        top: "46%",
        width: radius * 2,
        height: radius * 2,
        marginLeft: -radius,
        marginTop: -radius,
        borderRadius: "50%",
        border: `1px solid ${COLORS.accent}`,
        opacity,
        transform: `scale(${scale})`,
      }}
    />
  );
};

export const OutroScene: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  // Logo animation
  const logoSpring = spring({
    frame: Math.max(0, frame - 20),
    fps,
    config: { damping: 15, stiffness: 50, mass: 1.2 },
  });

  const logoOpacity = interpolate(logoSpring, [0, 1], [0, 1]);
  const logoScale = interpolate(logoSpring, [0, 1], [0.5, 1]);

  // Tagline animation
  const taglineSpring = spring({
    frame: Math.max(0, frame - 55),
    fps,
    config: { damping: 14, stiffness: 60, mass: 0.8 },
  });

  const taglineOpacity = interpolate(taglineSpring, [0, 1], [0, 1]);
  const taglineY = interpolate(taglineSpring, [0, 1], [20, 0]);

  // CTA animation
  const ctaOpacity = interpolate(frame, [80, 110], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // Final fade to black
  const finalFade = interpolate(frame, [180, 210], [1, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // Pulsing glow behind logo
  const glowPulse = interpolate(
    Math.sin(frame * 0.06),
    [-1, 1],
    [0.3, 0.6],
  );

  return (
    <AbsoluteFill style={{ opacity: finalFade }}>
      {/* Concentric rings */}
      {[120, 200, 300, 420].map((r, i) => (
        <ConcentricRing key={i} radius={r} delay={10 + i * 10} />
      ))}

      {/* Orbiting notes */}
      {Array.from({ length: 12 }, (_, i) => (
        <OrbitNote
          key={i}
          index={i}
          total={12}
          radius={280 + (i % 3) * 80}
          speed={1 + (i % 2) * 0.5}
          size={24 + (i % 3) * 8}
        />
      ))}

      {/* Center glow */}
      <div
        style={{
          position: "absolute",
          left: "50%",
          top: "42%",
          width: 400,
          height: 400,
          marginLeft: -200,
          marginTop: -200,
          borderRadius: "50%",
          background: `radial-gradient(circle, ${COLORS.accent}15, transparent)`,
          opacity: glowPulse,
        }}
      />

      {/* Logo */}
      <div
        style={{
          position: "absolute",
          left: "50%",
          top: "38%",
          transform: `translate(-50%, -50%) scale(${logoScale})`,
          opacity: logoOpacity,
          textAlign: "center",
        }}
      >
        <div
          style={{
            fontSize: 160,
            fontWeight: 800,
            color: COLORS.text,
            letterSpacing: -4,
            textShadow: `0 0 80px ${COLORS.noteGlow}, 0 0 160px rgba(255,215,0,0.15)`,
            fontFamily:
              'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
          }}
        >
          In
          <span style={{ color: COLORS.accent }}>Tono</span>
        </div>
      </div>

      {/* Tagline */}
      <div
        style={{
          position: "absolute",
          left: "50%",
          top: "54%",
          transform: `translate(-50%) translateY(${taglineY}px)`,
          opacity: taglineOpacity,
          textAlign: "center",
        }}
      >
        <div
          style={{
            fontSize: 42,
            fontWeight: 300,
            color: COLORS.text,
            letterSpacing: 6,
            fontFamily:
              'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
          }}
        >
          Impara.{" "}
          <span style={{ color: COLORS.accent, fontWeight: 600 }}>
            Pratica.
          </span>{" "}
          Suona.
        </div>
      </div>

      {/* Decorative line */}
      <div
        style={{
          position: "absolute",
          left: "50%",
          top: "62%",
          transform: "translateX(-50%)",
          width: interpolate(frame, [70, 100], [0, 200], {
            extrapolateLeft: "clamp",
            extrapolateRight: "clamp",
          }),
          height: 2,
          background: `linear-gradient(90deg, transparent, ${COLORS.accent}80, transparent)`,
        }}
      />

      {/* CTA */}
      <div
        style={{
          position: "absolute",
          left: "50%",
          top: "70%",
          transform: "translate(-50%)",
          opacity: ctaOpacity,
          textAlign: "center",
        }}
      >
        <div
          style={{
            fontSize: 24,
            color: COLORS.textMuted,
            letterSpacing: 3,
            textTransform: "uppercase",
            fontFamily:
              'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
          }}
        >
          Disponibile su iOS, Android e Desktop
        </div>
      </div>

      {/* Bottom musical note decoration */}
      <div
        style={{
          position: "absolute",
          bottom: 60,
          left: "50%",
          transform: "translateX(-50%)",
          display: "flex",
          gap: 40,
          opacity: interpolate(frame, [100, 130], [0, 0.3], {
            extrapolateLeft: "clamp",
            extrapolateRight: "clamp",
          }),
        }}
      >
        {MUSIC_SYMBOLS.slice(0, 4).map((s, i) => (
          <span
            key={i}
            style={{
              fontSize: 24,
              color: COLORS.accent,
            }}
          >
            {s}
          </span>
        ))}
      </div>
    </AbsoluteFill>
  );
};
