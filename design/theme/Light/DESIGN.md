---
name: 'Aetheric Resonance: Celestial Day'
colors:
  surface: '#fef7ff'
  surface-dim: '#ded7e4'
  surface-bright: '#fef7ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f8f1fe'
  surface-container: '#f2ebf8'
  surface-container-high: '#ede5f3'
  surface-container-highest: '#e7e0ed'
  on-surface: '#1d1a23'
  on-surface-variant: '#494454'
  inverse-surface: '#322f39'
  inverse-on-surface: '#f5eefb'
  outline: '#7b7486'
  outline-variant: '#cbc3d7'
  surface-tint: '#6d3bd7'
  primary: '#5517be'
  on-primary: '#ffffff'
  primary-container: '#6d3bd7'
  on-primary-container: '#e0d2ff'
  inverse-primary: '#d0bcff'
  secondary: '#006b60'
  on-secondary: '#ffffff'
  secondary-container: '#92f4e4'
  on-secondary-container: '#007166'
  tertiary: '#713800'
  on-tertiary: '#ffffff'
  tertiary-container: '#944b00'
  on-tertiary-container: '#ffd0b0'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e9ddff'
  primary-fixed-dim: '#d0bcff'
  on-primary-fixed: '#23005c'
  on-primary-fixed-variant: '#5417be'
  secondary-fixed: '#92f4e4'
  secondary-fixed-dim: '#75d7c8'
  on-secondary-fixed: '#00201c'
  on-secondary-fixed-variant: '#005048'
  tertiary-fixed: '#ffdcc5'
  tertiary-fixed-dim: '#ffb783'
  on-tertiary-fixed: '#301400'
  on-tertiary-fixed-variant: '#703700'
  background: '#fef7ff'
  on-background: '#1d1a23'
  surface-variant: '#e7e0ed'
  aether-teal: '#44E2CD'
  aether-purple: '#D0BCFF'
  pearlescent-white: '#F8F7FA'
  airy-grey: '#E4E1E9'
typography:
  display-lg:
    fontFamily: EB Garamond
    fontSize: 42px
    fontWeight: '500'
    lineHeight: '1.1'
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: EB Garamond
    fontSize: 32px
    fontWeight: '500'
    lineHeight: '1.2'
  headline-lg-mobile:
    fontFamily: EB Garamond
    fontSize: 28px
    fontWeight: '500'
    lineHeight: '1.2'
  title-md:
    fontFamily: Hanken Grotesk
    fontSize: 20px
    fontWeight: '600'
    lineHeight: '1.4'
  body-md:
    fontFamily: Hanken Grotesk
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.6'
  label-sm:
    fontFamily: Space Grotesk
    fontSize: 12px
    fontWeight: '500'
    lineHeight: '1.0'
    letterSpacing: 0.1em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 4px
  container-padding: 24px
  element-gap: 16px
  section-margin: 40px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 32px
---

## Brand & Style

This design system reimagines the "Cosmic Noir" aesthetic as a "Celestial Day" experience. It transitions from the mystery of the deep void to the clarity of a high-altitude sanctuary. The brand personality is scholarly, ethereal, and enlightened—evoking the feeling of a sun-drenched library in the clouds. It targets an audience that seeks mindfulness, high-fidelity audio, and intellectual depth.

The design style is a hybrid of **Glassmorphism** and **Minimalism**. It utilizes soft pearlescent whites, airy grays, and translucent "Aether" layers to create a sense of weightless verticality. Surfaces feel like polished quartz or frosted glass, illuminated by a soft, diffused light source that ensures maximum legibility while maintaining a mystical atmosphere.

## Colors

The color palette is built on a foundation of **Pearlescent White** and **Airy Grey**, ensuring a bright, accessible canvas that meets high-contrast standards.

- **Primary (Royal Aether):** A deepened version of the brand's purple (#6D3BD7) used for critical text and interactive triggers to ensure accessibility on light backgrounds.
- **Secondary (Deep Teal):** A rich, dark teal (#008174) used for secondary actions and success states.
- **Aether Gradients:** High-impact moments (like primary play buttons) utilize a vibrant gradient from **Aether Teal** to **Aether Purple**, representing a shimmering sunrise.
- **Neutral:** A spectrum of grays derived from the "on-surface" tones, providing soft contrast for borders and secondary text.

## Typography

The typography maintains a "Mystic-Modern" tension with a focus on readability against light surfaces.

- **Headlines:** **EB Garamond** serves as the primary display face. It provides a scholarly, literary weight. For this light theme, ensure headlines use the darkest neutral tones to maintain an authoritative presence.
- **Body:** **Hanken Grotesk** offers a clean, neutral counterpoint. The increased line height (1.6) aids in long-form reading of episode transcripts and descriptions.
- **Labels:** **Space Grotesk** is used for technical metadata, providing a sharp, futuristic contrast to the classicism of the serif headlines.

## Layout & Spacing

The layout philosophy is **Fluid and Expansive**. It relies on generous white space to simulate the feeling of an open, airy environment.

- **Grid:** A 12-column fluid grid for desktop, collapsing to a single column for mobile with a consistent **24px margin**.
- **Rhythm:** Vertical spacing is prioritized. Use `section-margin` (40px) between major content groups to prevent visual clutter and maintain the system’s calm, meditative tone.
- **Alignment:** While text is generally left-aligned for readability, display headers can be centered to emphasize their "Oracle" or "Sanctuary" status.

## Elevation & Depth

In the "Celestial Day" theme, depth is achieved through **Soft Ambient Shadows** and **Subtle Tonal Overlays** rather than glows.

- **Level 1 (Base):** Pearlescent White (#F8F7FA) with a very faint, light-grey grain texture.
- **Level 2 (Containers):** Airy Grey (#E4E1E9) or White with 40% opacity and a high-refraction backdrop blur (30px).
- **Level 3 (Interactive):** Elements feature a very soft, diffused shadow (Hex: #303036 at 8% opacity) to suggest they are floating just above the surface.
- **Level 4 (Overlay):** Floating elements like the Mini-Player use a thin 1px "Silver Stroke" (Airy Grey) and a more pronounced shadow to distinguish them from the background.

## Shapes

The shape language is **Organic and Welcoming**. 

- **Containers:** Standard containers use `rounded-lg` (1rem) to soften the interface.
- **Action Elements:** Buttons and interactive chips utilize `rounded-xl` (1.5rem) or full pill shapes to invite touch.
- **Imagery:** Album art and podcast covers should have the standard `rounded-lg` treatment to maintain consistency with the container logic.

## Components

### Buttons & Interaction
- **Primary Action:** A pill-shaped button with the Aether Gradient (Teal to Purple). Text should be white and bolded for maximum contrast.
- **Secondary Action:** Ghost buttons with a 1.5px Airy Grey border and Primary Purple text.
- **Chips:** Pill-shaped, light-grey backgrounds (#E4E1E9) that transition to the Aether Gradient on selection.

### Cards
- **Podcast Cards:** White glass containers with a subtle 1px Airy Grey border. On hover, the shadow depth increases slightly, and the border tint shifts toward the Aether Teal.

### Inputs
- **Search Bar:** A soft-grey recessed field. The focus state should highlight the border with a 2px Royal Aether stroke.

### Media Player
- **Progress Bar:** The track is a light Airy Grey, while the active "played" portion is a vibrant Teal-to-Purple gradient.
- **The "Oracle" Visualizer:** In the light theme, the concentric rings are semi-transparent and use the Aether Teal and Purple colors at 30% opacity, creating a subtle, shimmering ripple effect.