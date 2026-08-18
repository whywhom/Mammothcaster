---
name: Aetheric Resonance
colors:
  surface: '#131318'
  surface-dim: '#131318'
  surface-bright: '#39383e'
  surface-container-lowest: '#0e0e13'
  surface-container-low: '#1b1b20'
  surface-container: '#1f1f25'
  surface-container-high: '#2a292f'
  surface-container-highest: '#35343a'
  on-surface: '#e4e1e9'
  on-surface-variant: '#cbc3d7'
  inverse-surface: '#e4e1e9'
  inverse-on-surface: '#303036'
  outline: '#958ea0'
  outline-variant: '#494454'
  surface-tint: '#d0bcff'
  primary: '#d0bcff'
  on-primary: '#3c0091'
  primary-container: '#a078ff'
  on-primary-container: '#340080'
  inverse-primary: '#6d3bd7'
  secondary: '#44e2cd'
  on-secondary: '#003731'
  secondary-container: '#03c6b2'
  on-secondary-container: '#004d44'
  tertiary: '#d3beeb'
  on-tertiary: '#38294d'
  tertiary-container: '#9c89b3'
  on-tertiary-container: '#312246'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#e9ddff'
  primary-fixed-dim: '#d0bcff'
  on-primary-fixed: '#23005c'
  on-primary-fixed-variant: '#5516be'
  secondary-fixed: '#62fae3'
  secondary-fixed-dim: '#3cddc7'
  on-secondary-fixed: '#00201c'
  on-secondary-fixed-variant: '#005047'
  tertiary-fixed: '#eddcff'
  tertiary-fixed-dim: '#d3beeb'
  on-tertiary-fixed: '#231437'
  on-tertiary-fixed-variant: '#4f4065'
  background: '#131318'
  on-background: '#e4e1e9'
  surface-variant: '#35343a'
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
The design system is centered on a "Cosmic Noir" aesthetic—a blend of high-fidelity spiritualism and deep-space exploration. It evokes a sense of mystery, introspection, and sonic immersion.

The style leverages **Glassmorphism** and **Atmospheric Depth**. Surfaces are not solid; they are translucent veils that reveal glowing nebulae beneath. The interface should feel like a sophisticated ritual tool, utilizing soft blurs, radiant energy fields, and fluid organic transitions to guide the user through their auditory journey.

## Colors
The palette is rooted in the void of **Obsidian Black**, providing a high-contrast canvas for celestial accents. 

- **Primary (Neon Violet):** Used for interactive triggers, active states, and primary brand moments. It represents the "spark" of consciousness.
- **Secondary (Ethereal Teal):** Used for secondary information, success states, and subtle highlights. It provides a cooling balance to the violet.
- **Tertiary (Deep Space Purple):** The foundational layer for containers and large surfaces, creating a sense of infinite depth.
- **Gradients:** Use linear gradients (45deg) transitioning from Neon Violet to Ethereal Teal for high-impact elements like play buttons and progress bars.

## Typography
The typography system creates a "Mystic-Modern" tension. 

- **Headlines:** Utilize **EB Garamond** for its literary and authoritative character. It should feel etched into the interface.
- **Body:** **Hanken Grotesk** provides a clean, ultra-legible counterpoint for long descriptions and episode notes, ensuring no fatigue during reading.
- **Metadata/Labels:** **Space Grotesk** adds a technical, futuristic edge to timestamps and category tags, reinforcing the cosmic theme.

## Layout & Spacing
The layout follows a **Fluid Organic** model. While components align to a 4px soft-grid, the arrangement should feel spacious and "unbound."

- **Margins:** Standardize on a 24px side margin for mobile to allow the "glow" of cards to breathe without hitting the screen edge.
- **Rhythm:** Use generous vertical spacing (stack-lg) between distinct content sections (e.g., "Recently Played" vs "Recommended") to maintain a sense of calm.
- **Safe Areas:** Ensure interactive controls are positioned within the lower two-thirds of the screen for ergonomic thumb-access during one-handed use.

## Elevation & Depth
Depth is not communicated through traditional shadows, but through **Luminance and Translucency**.

- **Level 1 (Deepest):** Obsidian Black background with a subtle "Radial Grain" texture.
- **Level 2 (Containers):** Deep Space Purple with 60% opacity and a 20px backdrop blur.
- **Level 3 (Interactive):** Glass layers with a 1px "Inner Glow" border (rgba(255,255,255, 0.1)).
- **Level 4 (Active/Floating):** Elements like the Mini-Player use a "Bloom" effect—a soft, external Neon Violet glow (blur: 24px, spread: -4px) that makes the element appear to levitate above the content.

## Shapes
The shape language favors **Soft Geometry**. 

- **Cards:** Use `rounded-lg` (1rem) for most content containers to feel approachable yet structured.
- **Play Controls:** Use `rounded-xl` or full circles for primary actions to distinguish them from content.
- **Visual Flourish:** Incorporate "Blob" shapes in the background—large, low-opacity, animated organic paths that slowly shift color between Violet and Teal to simulate a living nebula.

## Components

### Cards & Items
- **Podcast Cards:** Glass containers with a 1px stroke. The stroke should use a subtle gradient. On-tap, the card border should "pulse" with a Neon Violet glow.
- **Episode List Items:** Clean layouts with Space Grotesk labels for duration. Use a secondary-colored "dot" to indicate unplayed status.

### Media Player Controls
- **Primary Play Button:** A large circular element with a dual-tone gradient (Violet to Teal). When playing, the button should have a "breathing" outer glow.
- **Progress Bar:** A thin Ethereal Teal line. The "played" portion should leave a faint glowing trail behind the seeker head.

### Inputs & Interaction
- **Search Bar:** A dark, semi-transparent field with a "glass" texture. The cursor should be Neon Violet.
- **Chips/Filters:** Pill-shaped outlines that fill with a solid Violet-to-Teal gradient when active.

### Distinctive Elements
- **The "Oracle" Visualizer:** A custom component in the player view consisting of fluid, concentric rings that react to the audio frequency, scaling and blurring in sync with the beat.