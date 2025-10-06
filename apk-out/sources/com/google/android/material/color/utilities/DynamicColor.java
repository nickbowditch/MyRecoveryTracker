package com.google.android.material.color.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

/* loaded from: classes.dex */
public final class DynamicColor {
    public final Function<DynamicScheme, DynamicColor> background;
    public final ContrastCurve contrastCurve;
    private final HashMap<DynamicScheme, Hct> hctCache;
    public final boolean isBackground;
    public final String name;
    public final Function<DynamicScheme, Double> opacity;
    public final Function<DynamicScheme, TonalPalette> palette;
    public final Function<DynamicScheme, DynamicColor> secondBackground;
    public final Function<DynamicScheme, Double> tone;
    public final Function<DynamicScheme, ToneDeltaPair> toneDeltaPair;

    public DynamicColor(String name, Function<DynamicScheme, TonalPalette> palette, Function<DynamicScheme, Double> tone, boolean isBackground, Function<DynamicScheme, DynamicColor> background, Function<DynamicScheme, DynamicColor> secondBackground, ContrastCurve contrastCurve, Function<DynamicScheme, ToneDeltaPair> toneDeltaPair) {
        this.hctCache = new HashMap<>();
        this.name = name;
        this.palette = palette;
        this.tone = tone;
        this.isBackground = isBackground;
        this.background = background;
        this.secondBackground = secondBackground;
        this.contrastCurve = contrastCurve;
        this.toneDeltaPair = toneDeltaPair;
        this.opacity = null;
    }

    public DynamicColor(String name, Function<DynamicScheme, TonalPalette> palette, Function<DynamicScheme, Double> tone, boolean isBackground, Function<DynamicScheme, DynamicColor> background, Function<DynamicScheme, DynamicColor> secondBackground, ContrastCurve contrastCurve, Function<DynamicScheme, ToneDeltaPair> toneDeltaPair, Function<DynamicScheme, Double> opacity) {
        this.hctCache = new HashMap<>();
        this.name = name;
        this.palette = palette;
        this.tone = tone;
        this.isBackground = isBackground;
        this.background = background;
        this.secondBackground = secondBackground;
        this.contrastCurve = contrastCurve;
        this.toneDeltaPair = toneDeltaPair;
        this.opacity = opacity;
    }

    public static DynamicColor fromPalette(String name, Function<DynamicScheme, TonalPalette> palette, Function<DynamicScheme, Double> tone) {
        return new DynamicColor(name, palette, tone, false, null, null, null, null);
    }

    public static DynamicColor fromPalette(String name, Function<DynamicScheme, TonalPalette> palette, Function<DynamicScheme, Double> tone, boolean isBackground) {
        return new DynamicColor(name, palette, tone, isBackground, null, null, null, null);
    }

    public static DynamicColor fromArgb(String name, int argb) {
        final Hct hct = Hct.fromInt(argb);
        final TonalPalette palette = TonalPalette.fromInt(argb);
        return fromPalette(name, new Function() { // from class: com.google.android.material.color.utilities.DynamicColor$$ExternalSyntheticLambda0
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return DynamicColor.lambda$fromArgb$0(palette, (DynamicScheme) obj);
            }
        }, new Function() { // from class: com.google.android.material.color.utilities.DynamicColor$$ExternalSyntheticLambda1
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return Double.valueOf(hct.getTone());
            }
        });
    }

    static /* synthetic */ TonalPalette lambda$fromArgb$0(TonalPalette palette, DynamicScheme s) {
        return palette;
    }

    public int getArgb(DynamicScheme scheme) {
        int argb = getHct(scheme).toInt();
        if (this.opacity == null) {
            return argb;
        }
        double percentage = this.opacity.apply(scheme).doubleValue();
        int alpha = MathUtils.clampInt(0, 255, (int) Math.round(255.0d * percentage));
        return (16777215 & argb) | (alpha << 24);
    }

    public Hct getHct(DynamicScheme scheme) {
        Hct cachedAnswer = this.hctCache.get(scheme);
        if (cachedAnswer != null) {
            return cachedAnswer;
        }
        double tone = getTone(scheme);
        Hct answer = this.palette.apply(scheme).getHct(tone);
        if (this.hctCache.size() > 4) {
            this.hctCache.clear();
        }
        this.hctCache.put(scheme, answer);
        return answer;
    }

    public double getTone(DynamicScheme scheme) {
        ArrayList<Double> availables;
        double nTone;
        double fTone;
        double nContrast;
        boolean decreasingContrast = scheme.contrastLevel < 0.0d;
        if (this.toneDeltaPair != null) {
            ToneDeltaPair toneDeltaPair = this.toneDeltaPair.apply(scheme);
            DynamicColor roleA = toneDeltaPair.getRoleA();
            DynamicColor roleB = toneDeltaPair.getRoleB();
            double delta = toneDeltaPair.getDelta();
            TonePolarity polarity = toneDeltaPair.getPolarity();
            boolean stayTogether = toneDeltaPair.getStayTogether();
            DynamicColor bg = this.background.apply(scheme);
            double bgTone = bg.getTone(scheme);
            boolean aIsNearer = polarity == TonePolarity.NEARER || (polarity == TonePolarity.LIGHTER && !scheme.isDark) || (polarity == TonePolarity.DARKER && scheme.isDark);
            DynamicColor nearer = aIsNearer ? roleA : roleB;
            DynamicColor farther = aIsNearer ? roleB : roleA;
            boolean amNearer = this.name.equals(nearer.name);
            double expansionDir = scheme.isDark ? 1.0d : -1.0d;
            boolean decreasingContrast2 = decreasingContrast;
            double nContrast2 = nearer.contrastCurve.getContrast(scheme.contrastLevel);
            double fContrast = farther.contrastCurve.getContrast(scheme.contrastLevel);
            double nInitialTone = nearer.tone.apply(scheme).doubleValue();
            if (Contrast.ratioOfTones(bgTone, nInitialTone) >= nContrast2) {
                nTone = nInitialTone;
            } else {
                nTone = foregroundTone(bgTone, nContrast2);
            }
            double fInitialTone = farther.tone.apply(scheme).doubleValue();
            if (Contrast.ratioOfTones(bgTone, fInitialTone) >= fContrast) {
                fTone = fInitialTone;
            } else {
                fTone = foregroundTone(bgTone, fContrast);
            }
            if (decreasingContrast2) {
                nTone = foregroundTone(bgTone, nContrast2);
                fTone = foregroundTone(bgTone, fContrast);
            }
            if ((fTone - nTone) * expansionDir >= delta) {
                nContrast = fTone;
            } else {
                double fTone2 = MathUtils.clampDouble(0.0d, 100.0d, nTone + (delta * expansionDir));
                if ((fTone2 - nTone) * expansionDir >= delta) {
                    nContrast = fTone2;
                } else {
                    nTone = MathUtils.clampDouble(0.0d, 100.0d, fTone2 - (delta * expansionDir));
                    nContrast = fTone2;
                }
            }
            if (50.0d > nTone || nTone >= 60.0d) {
                if (50.0d <= nContrast && nContrast < 60.0d) {
                    if (stayTogether) {
                        if (expansionDir > 0.0d) {
                            nTone = 60.0d;
                            nContrast = Math.max(nContrast, 60.0d + (delta * expansionDir));
                        } else {
                            nTone = 49.0d;
                            nContrast = Math.min(nContrast, 49.0d + (delta * expansionDir));
                        }
                    } else if (expansionDir > 0.0d) {
                        nContrast = 60.0d;
                    } else {
                        nContrast = 49.0d;
                    }
                }
            } else if (expansionDir > 0.0d) {
                nTone = 60.0d;
                nContrast = Math.max(nContrast, 60.0d + (delta * expansionDir));
            } else {
                nTone = 49.0d;
                nContrast = Math.min(nContrast, 49.0d + (delta * expansionDir));
            }
            return amNearer ? nTone : nContrast;
        }
        boolean decreasingContrast3 = decreasingContrast;
        double answer = this.tone.apply(scheme).doubleValue();
        if (this.background == null) {
            return answer;
        }
        double bgTone2 = this.background.apply(scheme).getTone(scheme);
        double desiredRatio = this.contrastCurve.getContrast(scheme.contrastLevel);
        if (Contrast.ratioOfTones(bgTone2, answer) < desiredRatio) {
            answer = foregroundTone(bgTone2, desiredRatio);
        }
        if (decreasingContrast3) {
            answer = foregroundTone(bgTone2, desiredRatio);
        }
        if (this.isBackground && 50.0d <= answer && answer < 60.0d) {
            if (Contrast.ratioOfTones(49.0d, bgTone2) >= desiredRatio) {
                answer = 49.0d;
            } else {
                answer = 60.0d;
            }
        }
        if (this.secondBackground != null) {
            double bgTone1 = this.background.apply(scheme).getTone(scheme);
            double bgTone22 = this.secondBackground.apply(scheme).getTone(scheme);
            double upper = Math.max(bgTone1, bgTone22);
            double lower = Math.min(bgTone1, bgTone22);
            if (Contrast.ratioOfTones(upper, answer) >= desiredRatio && Contrast.ratioOfTones(lower, answer) >= desiredRatio) {
                return answer;
            }
            double lightOption = Contrast.lighter(upper, desiredRatio);
            double darkOption = Contrast.darker(lower, desiredRatio);
            ArrayList<Double> availables2 = new ArrayList<>();
            if (lightOption == -1.0d) {
                availables = availables2;
            } else {
                availables = availables2;
                availables.add(Double.valueOf(lightOption));
            }
            if (darkOption != -1.0d) {
                availables.add(Double.valueOf(darkOption));
            }
            boolean prefersLight = tonePrefersLightForeground(bgTone1) || tonePrefersLightForeground(bgTone22);
            if (prefersLight) {
                if (lightOption == -1.0d) {
                    return 100.0d;
                }
                return lightOption;
            }
            if (availables.size() == 1) {
                return availables.get(0).doubleValue();
            }
            if (darkOption == -1.0d) {
                return 0.0d;
            }
            return darkOption;
        }
        return answer;
    }

    public static double foregroundTone(double bgTone, double ratio) {
        double lighterTone = Contrast.lighterUnsafe(bgTone, ratio);
        double darkerTone = Contrast.darkerUnsafe(bgTone, ratio);
        double lighterRatio = Contrast.ratioOfTones(lighterTone, bgTone);
        double darkerRatio = Contrast.ratioOfTones(darkerTone, bgTone);
        boolean preferLighter = tonePrefersLightForeground(bgTone);
        if (!preferLighter) {
            return (darkerRatio >= ratio || darkerRatio >= lighterRatio) ? darkerTone : lighterTone;
        }
        boolean negligibleDifference = Math.abs(lighterRatio - darkerRatio) < 0.1d && lighterRatio < ratio && darkerRatio < ratio;
        if (lighterRatio >= ratio || lighterRatio >= darkerRatio || negligibleDifference) {
            return lighterTone;
        }
        return darkerTone;
    }

    public static double enableLightForeground(double tone) {
        if (tonePrefersLightForeground(tone) && !toneAllowsLightForeground(tone)) {
            return 49.0d;
        }
        return tone;
    }

    public static boolean tonePrefersLightForeground(double tone) {
        return Math.round(tone) < 60;
    }

    public static boolean toneAllowsLightForeground(double tone) {
        return Math.round(tone) <= 49;
    }
}
