import {
  AbsoluteFill,
  interpolate,
  spring,
  useCurrentFrame,
  useVideoConfig,
} from "remotion";
import { COLORS, FEATURES, INSTRUMENTS } from "../lib/constants";

const FeatureCard: React.FC<{
  icon: string;
  title: string;
  desc: string;
  color: string;
  index: number;
  totalCards: number;
}> = ({ icon, title, desc, color, index, totalCards }) => {
  const frame = useCurrentFrame();
  const { fps, width } = useVideoConfig();

  const entryDelay = 40 + index * 20;

  const cardSpring = spring({
    frame: Math.max(0, frame - entryDelay),
    fps,
    config: { damping: 14, stiffness: 80, mass: 0.8 },
  });

  const opacity = interpolate(cardSpring, [0, 1], [0, 1]);
  const translateY = interpolate(cardSpring, [0, 1], [80, 0]);
  const scale = interpolate(cardSpring, [0, 1], [0.8, 1]);

  const cardWidth = 380;
  const gap = 30;
  const totalWidth = totalCards * cardWidth + (totalCards - 1) * gap;
  const startX = (width - totalWidth) / 2;
  const x = startX + index * (cardWidth + gap);

  // Subtle floating animation
  const floatY = Math.sin(frame * 0.04 + index * 1.5) * 5;

  // Hover glow
  const glowPulse = interpolate(
    Math.sin(frame * 0.06 + index),
    [-1, 1],
    [0.3, 0.6],
  );

  return (
    <div
      style={{
        position: "absolute",
        left: x,
        top: 340 + translateY + floatY,
        width: cardWidth,
        opacity,
        transform: `scale(${scale})`,
      }}
    >
      {/* Card background */}
      <div
        style={{
          background: COLORS.cardBg,
          backdropFilter: "blur(20px)",
          border: `1px solid ${COLORS.cardBorder}`,
          borderRadius: 24,
          padding: "50px 36px",
          textAlign: "center",
          position: "relative",
          overflow: "hidden",
        }}
      >
        {/* Top accent line */}
        <div
          style={{
            position: "absolute",
            top: 0,
            left: "10%",
            right: "10%",
            height: 3,
            background: `linear-gradient(90deg, transparent, ${color}, transparent)`,
            borderRadius: 2,
          }}
        />

        {/* Glow effect */}
        <div
          style={{
            position: "absolute",
            top: -50,
            left: "50%",
            transform: "translateX(-50%)",
            width: 200,
            height: 200,
            borderRadius: "50%",
            background: `radial-gradient(circle, ${color}15, transparent)`,
            opacity: glowPulse,
          }}
        />

        {/* Icon */}
        <div
          style={{
            fontSize: 64,
            marginBottom: 20,
            filter: `drop-shadow(0 0 15px ${color}50)`,
          }}
        >
          {icon}
        </div>

        {/* Title */}
        <div
          style={{
            fontSize: 30,
            fontWeight: 700,
            color: COLORS.text,
            marginBottom: 12,
            fontFamily:
              'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
          }}
        >
          {title}
        </div>

        {/* Description */}
        <div
          style={{
            fontSize: 18,
            color: COLORS.textMuted,
            lineHeight: 1.5,
            fontFamily:
              'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
          }}
        >
          {desc}
        </div>
      </div>
    </div>
  );
};

const InstrumentBadge: React.FC<{
  name: string;
  emoji: string;
  index: number;
  total: number;
}> = ({ name, emoji, index, total }) => {
  const frame = useCurrentFrame();
  const { fps, width } = useVideoConfig();

  const entryDelay = 160 + index * 12;

  const badgeSpring = spring({
    frame: Math.max(0, frame - entryDelay),
    fps,
    config: { damping: 12, stiffness: 100, mass: 0.5 },
  });

  const opacity = interpolate(badgeSpring, [0, 1], [0, 1]);
  const scale = interpolate(badgeSpring, [0, 1], [0.5, 1]);

  const gap = 20;
  const badgeWidth = 200;
  const totalWidth = total * badgeWidth + (total - 1) * gap;
  const startX = (width - totalWidth) / 2;
  const x = startX + index * (badgeWidth + gap);

  return (
    <div
      style={{
        position: "absolute",
        left: x,
        top: 780,
        width: badgeWidth,
        opacity,
        transform: `scale(${scale})`,
        textAlign: "center",
      }}
    >
      <div
        style={{
          background: "rgba(255,255,255,0.05)",
          border: `1px solid rgba(255,255,255,0.1)`,
          borderRadius: 16,
          padding: "16px 12px",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          gap: 10,
        }}
      >
        <span style={{ fontSize: 28 }}>{emoji}</span>
        <span
          style={{
            fontSize: 18,
            color: COLORS.text,
            fontWeight: 500,
            fontFamily:
              'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
          }}
        >
          {name}
        </span>
      </div>
    </div>
  );
};

export const FeaturesScene: React.FC = () => {
  const frame = useCurrentFrame();

  // Scene fade
  const fadeIn = interpolate(frame, [0, 30], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const fadeOut = interpolate(frame, [270, 300], [1, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // Title
  const titleOpacity = interpolate(frame, [10, 35], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const titleY = interpolate(frame, [10, 35], [30, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // Instruments section title
  const instrTitleOpacity = interpolate(frame, [140, 165], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

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
          textAlign: "center",
        }}
      >
        <div
          style={{
            fontSize: 52,
            fontWeight: 700,
            color: COLORS.text,
            fontFamily:
              'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
          }}
        >
          Tutto ciò di cui hai bisogno
        </div>
        <div
          style={{
            fontSize: 22,
            color: COLORS.textMuted,
            marginTop: 10,
            fontFamily:
              'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
          }}
        >
          Strumenti professionali per ogni musicista
        </div>
      </div>

      {/* Decorative line under title */}
      <div
        style={{
          position: "absolute",
          left: "50%",
          top: 260,
          transform: "translateX(-50%)",
          width: interpolate(frame, [30, 60], [0, 120], {
            extrapolateLeft: "clamp",
            extrapolateRight: "clamp",
          }),
          height: 3,
          background: `linear-gradient(90deg, transparent, ${COLORS.accent}, transparent)`,
          borderRadius: 2,
        }}
      />

      {/* Feature cards */}
      {FEATURES.map((feature, i) => (
        <FeatureCard
          key={i}
          icon={feature.icon}
          title={feature.title}
          desc={feature.desc}
          color={feature.color}
          index={i}
          totalCards={FEATURES.length}
        />
      ))}

      {/* Instruments section title */}
      <div
        style={{
          position: "absolute",
          left: "50%",
          top: 720,
          transform: "translateX(-50%)",
          opacity: instrTitleOpacity,
          fontSize: 28,
          fontWeight: 600,
          color: COLORS.textMuted,
          letterSpacing: 4,
          textTransform: "uppercase",
          fontFamily:
            'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
        }}
      >
        Strumenti Supportati
      </div>

      {/* Instrument badges */}
      {INSTRUMENTS.map((instr, i) => (
        <InstrumentBadge
          key={i}
          name={instr.name}
          emoji={instr.emoji}
          index={i}
          total={INSTRUMENTS.length}
        />
      ))}
    </AbsoluteFill>
  );
};
