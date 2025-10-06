package androidx.core.text.util;

import java.util.Locale;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* loaded from: classes.dex */
class FindAddress {
    private static final String HOUSE_COMPONENT = "(?:one|[0-9]+([a-z](?=[^a-z]|$)|st|nd|rd|th)?)";
    private static final String HOUSE_END = "(?=[,\"'\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000\n\u000b\f\r\u0085\u2028\u2029]|$)";
    private static final String HOUSE_POST_DELIM = ",\"'\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000\n\u000b\f\r\u0085\u2028\u2029";
    private static final String HOUSE_PRE_DELIM = ":,\"'\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000\n\u000b\f\r\u0085\u2028\u2029";
    private static final int MAX_ADDRESS_LINES = 5;
    private static final int MAX_ADDRESS_WORDS = 14;
    private static final int MAX_LOCATION_NAME_DISTANCE = 5;
    private static final int MIN_ADDRESS_WORDS = 4;
    private static final String NL = "\n\u000b\f\r\u0085\u2028\u2029";
    private static final String SP = "\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000";
    private static final String WORD_DELIM = ",*•\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000\n\u000b\f\r\u0085\u2028\u2029";
    private static final String WORD_END = "(?=[,*•\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000\n\u000b\f\r\u0085\u2028\u2029]|$)";
    private static final String WS = "\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000\n\u000b\f\r\u0085\u2028\u2029";
    private static final int kMaxAddressNameWordLength = 25;
    private static final ZipRange[] sStateZipCodeRanges = {new ZipRange(99, 99, -1, -1), new ZipRange(35, 36, -1, -1), new ZipRange(71, 72, -1, -1), new ZipRange(96, 96, -1, -1), new ZipRange(85, 86, -1, -1), new ZipRange(90, 96, -1, -1), new ZipRange(80, 81, -1, -1), new ZipRange(6, 6, -1, -1), new ZipRange(20, 20, -1, -1), new ZipRange(19, 19, -1, -1), new ZipRange(32, 34, -1, -1), new ZipRange(96, 96, -1, -1), new ZipRange(30, 31, -1, -1), new ZipRange(96, 96, -1, -1), new ZipRange(96, 96, -1, -1), new ZipRange(50, 52, -1, -1), new ZipRange(83, 83, -1, -1), new ZipRange(60, 62, -1, -1), new ZipRange(46, 47, -1, -1), new ZipRange(66, 67, 73, -1), new ZipRange(40, 42, -1, -1), new ZipRange(70, 71, -1, -1), new ZipRange(1, 2, -1, -1), new ZipRange(20, 21, -1, -1), new ZipRange(3, 4, -1, -1), new ZipRange(96, 96, -1, -1), new ZipRange(48, 49, -1, -1), new ZipRange(55, 56, -1, -1), new ZipRange(63, 65, -1, -1), new ZipRange(96, 96, -1, -1), new ZipRange(38, 39, -1, -1), new ZipRange(55, 56, -1, -1), new ZipRange(27, 28, -1, -1), new ZipRange(58, 58, -1, -1), new ZipRange(68, 69, -1, -1), new ZipRange(3, 4, -1, -1), new ZipRange(7, 8, -1, -1), new ZipRange(87, 88, 86, -1), new ZipRange(88, 89, 96, -1), new ZipRange(10, 14, 0, 6), new ZipRange(43, 45, -1, -1), new ZipRange(73, 74, -1, -1), new ZipRange(97, 97, -1, -1), new ZipRange(15, 19, -1, -1), new ZipRange(6, 6, 0, 9), new ZipRange(96, 96, -1, -1), new ZipRange(2, 2, -1, -1), new ZipRange(29, 29, -1, -1), new ZipRange(57, 57, -1, -1), new ZipRange(37, 38, -1, -1), new ZipRange(75, 79, 87, 88), new ZipRange(84, 84, -1, -1), new ZipRange(22, 24, 20, -1), new ZipRange(6, 9, -1, -1), new ZipRange(5, 5, -1, -1), new ZipRange(98, 99, -1, -1), new ZipRange(53, 54, -1, -1), new ZipRange(24, 26, -1, -1), new ZipRange(82, 83, -1, -1)};
    private static final Pattern sWordRe = Pattern.compile("[^,*•\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000\n\u000b\f\r\u0085\u2028\u2029]+(?=[,*•\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000\n\u000b\f\r\u0085\u2028\u2029]|$)", 2);
    private static final Pattern sHouseNumberRe = Pattern.compile("(?:one|[0-9]+([a-z](?=[^a-z]|$)|st|nd|rd|th)?)(?:-(?:one|[0-9]+([a-z](?=[^a-z]|$)|st|nd|rd|th)?))*(?=[,\"'\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000\n\u000b\f\r\u0085\u2028\u2029]|$)", 2);
    private static final Pattern sStateRe = Pattern.compile("(?:(ak|alaska)|(al|alabama)|(ar|arkansas)|(as|american[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+samoa)|(az|arizona)|(ca|california)|(co|colorado)|(ct|connecticut)|(dc|district[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+of[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+columbia)|(de|delaware)|(fl|florida)|(fm|federated[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+states[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+of[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+micronesia)|(ga|georgia)|(gu|guam)|(hi|hawaii)|(ia|iowa)|(id|idaho)|(il|illinois)|(in|indiana)|(ks|kansas)|(ky|kentucky)|(la|louisiana)|(ma|massachusetts)|(md|maryland)|(me|maine)|(mh|marshall[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+islands)|(mi|michigan)|(mn|minnesota)|(mo|missouri)|(mp|northern[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+mariana[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+islands)|(ms|mississippi)|(mt|montana)|(nc|north[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+carolina)|(nd|north[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+dakota)|(ne|nebraska)|(nh|new[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+hampshire)|(nj|new[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+jersey)|(nm|new[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+mexico)|(nv|nevada)|(ny|new[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+york)|(oh|ohio)|(ok|oklahoma)|(or|oregon)|(pa|pennsylvania)|(pr|puerto[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+rico)|(pw|palau)|(ri|rhode[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+island)|(sc|south[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+carolina)|(sd|south[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+dakota)|(tn|tennessee)|(tx|texas)|(ut|utah)|(va|virginia)|(vi|virgin[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+islands)|(vt|vermont)|(wa|washington)|(wi|wisconsin)|(wv|west[\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000]+virginia)|(wy|wyoming))(?=[,*•\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000\n\u000b\f\r\u0085\u2028\u2029]|$)", 2);
    private static final Pattern sLocationNameRe = Pattern.compile("(?:alley|annex|arcade|ave[.]?|avenue|alameda|bayou|beach|bend|bluffs?|bottom|boulevard|branch|bridge|brooks?|burgs?|bypass|broadway|camino|camp|canyon|cape|causeway|centers?|circles?|cliffs?|club|common|corners?|course|courts?|coves?|creek|crescent|crest|crossing|crossroad|curve|circulo|dale|dam|divide|drives?|estates?|expressway|extensions?|falls?|ferry|fields?|flats?|fords?|forest|forges?|forks?|fort|freeway|gardens?|gateway|glens?|greens?|groves?|harbors?|haven|heights|highway|hills?|hollow|inlet|islands?|isle|junctions?|keys?|knolls?|lakes?|land|landing|lane|lights?|loaf|locks?|lodge|loop|mall|manors?|meadows?|mews|mills?|mission|motorway|mount|mountains?|neck|orchard|oval|overpass|parks?|parkways?|pass|passage|path|pike|pines?|plains?|plaza|points?|ports?|prairie|privada|radial|ramp|ranch|rapids?|rd[.]?|rest|ridges?|river|roads?|route|row|rue|run|shoals?|shores?|skyway|springs?|spurs?|squares?|station|stravenue|stream|st[.]?|streets?|summit|speedway|terrace|throughway|trace|track|trafficway|trail|tunnel|turnpike|underpass|unions?|valleys?|viaduct|views?|villages?|ville|vista|walks?|wall|ways?|wells?|xing|xrd)(?=[,*•\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000\n\u000b\f\r\u0085\u2028\u2029]|$)", 2);
    private static final Pattern sSuffixedNumberRe = Pattern.compile("([0-9]+)(st|nd|rd|th)", 2);
    private static final Pattern sZipCodeRe = Pattern.compile("(?:[0-9]{5}(?:-[0-9]{4})?)(?=[,*•\t  \u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006 \u2008\u2009\u200a \u205f\u3000\n\u000b\f\r\u0085\u2028\u2029]|$)", 2);

    private static class ZipRange {
        int mException1;
        int mException2;
        int mHigh;
        int mLow;

        ZipRange(int low, int high, int exception1, int exception2) {
            this.mLow = low;
            this.mHigh = high;
            this.mException1 = exception1;
            this.mException2 = exception2;
        }

        boolean matches(String zipCode) throws NumberFormatException {
            int prefix = Integer.parseInt(zipCode.substring(0, 2));
            return (this.mLow <= prefix && prefix <= this.mHigh) || prefix == this.mException1 || prefix == this.mException2;
        }
    }

    private static boolean checkHouseNumber(String houseNumber) throws NumberFormatException {
        int digitCount = 0;
        for (int i = 0; i < houseNumber.length(); i++) {
            if (Character.isDigit(houseNumber.charAt(i))) {
                digitCount++;
            }
        }
        if (digitCount > 5) {
            return false;
        }
        Matcher suffixMatcher = sSuffixedNumberRe.matcher(houseNumber);
        if (!suffixMatcher.find()) {
            return true;
        }
        int num = Integer.parseInt(suffixMatcher.group(1));
        if (num == 0) {
            return false;
        }
        String suffix = suffixMatcher.group(2).toLowerCase(Locale.getDefault());
        switch (num % 10) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
        }
        return false;
    }

    public static MatchResult matchHouseNumber(String content, int offset) {
        if (offset > 0 && HOUSE_PRE_DELIM.indexOf(content.charAt(offset - 1)) == -1) {
            return null;
        }
        Matcher matcher = sHouseNumberRe.matcher(content).region(offset, content.length());
        if (matcher.lookingAt()) {
            MatchResult matchResult = matcher.toMatchResult();
            if (checkHouseNumber(matchResult.group(0))) {
                return matchResult;
            }
        }
        return null;
    }

    public static MatchResult matchState(String content, int offset) {
        if (offset > 0 && WORD_DELIM.indexOf(content.charAt(offset - 1)) == -1) {
            return null;
        }
        Matcher stateMatcher = sStateRe.matcher(content).region(offset, content.length());
        if (stateMatcher.lookingAt()) {
            return stateMatcher.toMatchResult();
        }
        return null;
    }

    private static boolean isValidZipCode(String zipCode, MatchResult stateMatch) {
        if (stateMatch == null) {
            return false;
        }
        int stateIndex = stateMatch.groupCount();
        while (true) {
            if (stateIndex <= 0) {
                break;
            }
            int stateIndex2 = stateIndex - 1;
            if (stateMatch.group(stateIndex) != null) {
                stateIndex = stateIndex2;
                break;
            }
            stateIndex = stateIndex2;
        }
        return sZipCodeRe.matcher(zipCode).matches() && sStateZipCodeRanges[stateIndex].matches(zipCode);
    }

    public static boolean isValidZipCode(String zipCode, String state) {
        return isValidZipCode(zipCode, matchState(state, 0));
    }

    public static boolean isValidZipCode(String zipCode) {
        return sZipCodeRe.matcher(zipCode).matches();
    }

    public static boolean isValidLocationName(String location) {
        return sLocationNameRe.matcher(location).matches();
    }

    /* JADX WARN: Code restructure failed: missing block: B:58:0x00d6, code lost:
    
        if (r1 <= 0) goto L60;
     */
    /* JADX WARN: Code restructure failed: missing block: B:59:0x00d8, code lost:
    
        return r1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:60:0x00d9, code lost:
    
        if (r0 <= 0) goto L62;
     */
    /* JADX WARN: Code restructure failed: missing block: B:61:0x00db, code lost:
    
        r9 = r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:62:0x00dd, code lost:
    
        r9 = r2;
     */
    /* JADX WARN: Code restructure failed: missing block: B:64:0x00df, code lost:
    
        return -r9;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private static int attemptMatch(java.lang.String r13, java.util.regex.MatchResult r14) {
        /*
            r0 = -1
            r1 = -1
            int r2 = r14.end()
            r3 = 1
            r4 = 1
            r5 = 0
            r6 = 1
            java.lang.String r7 = ""
            java.util.regex.Pattern r8 = androidx.core.text.util.FindAddress.sWordRe
            java.util.regex.Matcher r8 = r8.matcher(r13)
        L12:
            int r9 = r13.length()
            if (r2 >= r9) goto Ld6
            boolean r9 = r8.find(r2)
            if (r9 != 0) goto L24
            int r9 = r13.length()
            int r9 = -r9
            return r9
        L24:
            int r9 = r8.end()
            int r10 = r8.start()
            int r9 = r9 - r10
            r10 = 25
            if (r9 <= r10) goto L37
            int r9 = r8.end()
            int r9 = -r9
            return r9
        L37:
            int r9 = r8.start()
            r10 = -1
            if (r2 >= r9) goto L50
            int r9 = r2 + 1
            char r2 = r13.charAt(r2)
            java.lang.String r11 = "\n\u000b\f\r\u0085\u2028\u2029"
            int r2 = r11.indexOf(r2)
            if (r2 == r10) goto L4e
            int r3 = r3 + 1
        L4e:
            r2 = r9
            goto L37
        L50:
            r9 = 5
            if (r3 <= r9) goto L55
            goto Ld6
        L55:
            int r6 = r6 + 1
            r11 = 14
            if (r6 <= r11) goto L5d
            goto Ld6
        L5d:
            java.util.regex.MatchResult r11 = matchHouseNumber(r13, r2)
            r12 = 0
            if (r11 == 0) goto L6f
            if (r4 == 0) goto L6b
            r9 = 1
            if (r3 <= r9) goto L6b
            int r9 = -r2
            return r9
        L6b:
            if (r0 != r10) goto Lcc
            r0 = r2
            goto Lcc
        L6f:
            r4 = 0
            java.lang.String r10 = r8.group(r12)
            boolean r10 = isValidLocationName(r10)
            if (r10 == 0) goto L7c
            r5 = 1
            goto Lcc
        L7c:
            if (r6 != r9) goto L85
            if (r5 != 0) goto L85
            int r2 = r8.end()
            goto Ld6
        L85:
            if (r5 == 0) goto Lcc
            r9 = 4
            if (r6 <= r9) goto Lcc
            java.util.regex.MatchResult r9 = matchState(r13, r2)
            if (r9 == 0) goto Lcc
            java.lang.String r10 = "et"
            boolean r10 = r7.equals(r10)
            if (r10 == 0) goto La9
            java.lang.String r10 = r9.group(r12)
            java.lang.String r11 = "al"
            boolean r10 = r10.equals(r11)
            if (r10 == 0) goto La9
            int r2 = r9.end()
            goto Ld6
        La9:
            java.util.regex.Pattern r10 = androidx.core.text.util.FindAddress.sWordRe
            java.util.regex.Matcher r10 = r10.matcher(r13)
            int r11 = r9.end()
            boolean r11 = r10.find(r11)
            if (r11 == 0) goto Lc8
            java.lang.String r11 = r10.group(r12)
            boolean r11 = isValidZipCode(r11, r9)
            if (r11 == 0) goto Lcc
            int r11 = r10.end()
            return r11
        Lc8:
            int r1 = r9.end()
        Lcc:
            java.lang.String r7 = r8.group(r12)
            int r2 = r8.end()
            goto L12
        Ld6:
            if (r1 <= 0) goto Ld9
            return r1
        Ld9:
            if (r0 <= 0) goto Ldd
            r9 = r0
            goto Lde
        Ldd:
            r9 = r2
        Lde:
            int r9 = -r9
            return r9
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.core.text.util.FindAddress.attemptMatch(java.lang.String, java.util.regex.MatchResult):int");
    }

    static String findAddress(String content) {
        Matcher houseNumberMatcher = sHouseNumberRe.matcher(content);
        int start = 0;
        while (houseNumberMatcher.find(start)) {
            if (checkHouseNumber(houseNumberMatcher.group(0))) {
                int start2 = houseNumberMatcher.start();
                int end = attemptMatch(content, houseNumberMatcher);
                if (end > 0) {
                    return content.substring(start2, end);
                }
                start = -end;
            } else {
                start = houseNumberMatcher.end();
            }
        }
        return null;
    }

    private FindAddress() {
    }
}
