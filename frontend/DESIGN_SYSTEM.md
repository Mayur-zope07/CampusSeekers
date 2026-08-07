# CampusSeekers Design System (Master Foundation)

Welcome to the Design System documentation for CampusSeekers. This system defines the colors, typography, spacing, motion behaviors, and reusable components of our interface, inspired by premium, minimalist SaaS platforms like Apple, Linear, Arc, and Stripe.

---

## 1. Brand & Design Philosophy

The visual identity of CampusSeekers is built on:
- **Grayscale First**: 95% of the interface is monochrome to feel premium and luxurious. Accent colors are used sparingly (5%) for notifications, key states, or AI features.
- **Floating & Layered**: Soft glass panels, blurs, and hover reflections give the layout visual depth.
- **Tactile Transitions**: Components respond instantly with physics-based spring animations.
- **Spacious & Balanced**: Alignment is strictly governed by our 8-point spacing scale.

---

## 2. Color System & Tokens

Our colors are defined in custom Tailwind theme variables:

| Variable Name | Hex / RGBA Value | Usage |
| :--- | :--- | :--- |
| `primary-bg` | `#050505` | Canvas background |
| `secondary-bg` | `#0B0B0E` | Sidebar, card bodies |
| `surface` | `rgba(255, 255, 255, 0.05)` | Regular panels |
| `surface-elevated` | `rgba(255, 255, 255, 0.08)` | Hover surfaces, buttons |
| `border-color` | `rgba(255, 255, 255, 0.08)` | Divider lines, boundaries |
| `text-primary` | `#FFFFFF` | Headings, active states |
| `text-secondary` | `#9E9E9F` | Paragraphs, labels |
| `text-tertiary` | `#6C6C6D` | Subtitles, help texts |
| `accent-cyan` | `#00F0FF` | Primary action clicks, searches |
| `accent-purple` | `#8A2BE2` | AI features, recommendations |
| `accent-green` | `#39FF14` | Successful validations, seats |
| `accent-orange` | `#FF5E00` | Warning, critical error alerts |

---

## 3. Typography Scale

We use four font families dynamically linked via standard Next.js loaders:

- **Orbitron (`font-futuristic`)**: Futuristic ultra-light font used strictly for logos, branding, and major headers. Characterized by extra-wide letter-spacing (`tracking-[0.25em]`).
- **Inter (`font-inter`)**: Modern, high-readability body typeface used for content, tables, and form fields.
- **Geist / Manrope**: Auxiliary clean grotesque typefaces.

---

## 4. Spacing Scale (8-Point System)

To ensure consistent alignment, all margins and paddings must align to this system:

- **`scale-xs` (8px)**: Internal component content spacing.
- **`scale-sm` (16px)**: Small elements gap, child paddings.
- **`scale-md` (24px)**: Grid gaps, medium card paddings.
- **`scale-lg` (32px)**: Outer section gutters, container margins.
- **`scale-xl` (48px)**: Large hero section stack gaps.

---

## 5. Glassmorphism Scale

Avoid flat boxes. Use our custom backdrop-blur variables:

* **`glass-xs`**: Backdrop blur `2px`. Used for hover popovers.
* **`glass-sm`**: Backdrop blur `4px`. Default for small input elements.
* **`glass-md`**: Backdrop blur `8px`. Default for cards and panels.
* **`glass-lg`**: Backdrop blur `16px`. Default for sidebars and settings columns.
* **`glass-navbar`**: Backdrop blur `8px` combined with a dark translucent tint.
* **`glass-dialog`**: Backdrop blur `40px`. High-opacity overlays.

---

## 6. Motion & Spring Values

We avoid linear page animations. Transitions utilize Framer Motion spring physics:

* **Stiff Click Spring**: `stiffness: 350`, `damping: 25` (tactile button tap scaling).
* **Medium Entry Spring**: `stiffness: 300`, `damping: 28` (sliding tabs and dropdown panels).
* **Smooth Follower Spring**: `stiffness: 180`, `damping: 65` (custom cursor ambient follower).
* **Ease Curve**: `easeOutExpo` `[0.16, 1, 0.3, 1]` (scroll-reveal transitions).

---

## 7. Folder Architecture

The frontend is organized inside `src/` to guarantee scaling capability:
```
src/
├── app/                  # Next.js App Router entrypoints & layouts
├── components/
│   ├── animations/       # CustomCursor, LenisProvider, Magnetic Attractors
│   ├── layout/           # Fixed Navbar, Sidebar panels
│   ├── ui/               # Reusable atomic controls (Buttons, Cards, Inputs, Dialogs)
│   ├── dashboard/        # Component segments for Dashboard
│   ├── search/           # College search modules
│   ├── recommendation/   # Smart Match layouts
│   └── workflow/         # Student trackers & wishlists
├── hooks/                # Custom React hooks (useToast)
├── providers/            # Context layout wrappers (QueryClient, Lenis, Toast)
├── styles/               # Global reset stylesheets (globals.css)
├── utils/                # Small utility routines (cn)
└── types/                # TypeScript models & response shapes
```

---

## 8. Extension Rules (Do's and Don't's)

### Do:
- Wrap interactive button containers in `<Magnetic>` to enhance tactile desktop experiences.
- Make all cards utilize the glowing `<Card>` component to render custom reflections tracking mouse locations.
- Verify elements slide and fade into view during scrolls using `<ScrollReveal>`.

### Don't:
- **Never hardcode hex values** for colors. Always use tailwind variables (e.g. `bg-primary-bg`, `text-accent-cyan`).
- **Never mix button heights** arbitrarily. Rely on small (`sm`), medium (`md`), or large (`lg`) class presets.
- Avoid introducing third-party modal managers. Use the design system's `<Dialog>` or `<Drawer>` controls to keep modal aesthetics consistent.
