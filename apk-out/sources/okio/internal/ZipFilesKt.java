package okio.internal;

import androidx.constraintlayout.widget.ConstraintLayout;
import java.io.IOException;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import kotlin.Metadata;
import kotlin.TuplesKt;
import kotlin.UShort;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.comparisons.ComparisonsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlin.text.CharsKt;
import kotlin.text.StringsKt;
import okhttp3.internal.ws.WebSocketProtocol;
import okio.BufferedSource;
import okio.FileMetadata;
import okio.FileSystem;
import okio.Path;
import okio.ZipFileSystem;

/* compiled from: ZipFiles.kt */
@Metadata(d1 = {"\u0000j\n\u0000\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\u001a\"\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00150\u00132\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00150\u0017H\u0002\u001a\u001f\u0010\u0018\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0019\u001a\u00020\u00012\u0006\u0010\u001a\u001a\u00020\u0001H\u0002¢\u0006\u0002\u0010\u001b\u001a.\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u00142\u0006\u0010\u001f\u001a\u00020 2\u0014\b\u0002\u0010!\u001a\u000e\u0012\u0004\u0012\u00020\u0015\u0012\u0004\u0012\u00020#0\"H\u0000\u001a\f\u0010$\u001a\u00020\u0015*\u00020%H\u0000\u001a\f\u0010&\u001a\u00020'*\u00020%H\u0002\u001a.\u0010(\u001a\u00020)*\u00020%2\u0006\u0010*\u001a\u00020\u00012\u0018\u0010+\u001a\u0014\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020)0,H\u0002\u001a\u0014\u0010-\u001a\u00020.*\u00020%2\u0006\u0010/\u001a\u00020.H\u0000\u001a\u0018\u00100\u001a\u0004\u0018\u00010.*\u00020%2\b\u0010/\u001a\u0004\u0018\u00010.H\u0002\u001a\u0014\u00101\u001a\u00020'*\u00020%2\u0006\u00102\u001a\u00020'H\u0002\u001a\f\u00103\u001a\u00020)*\u00020%H\u0000\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082T¢\u0006\u0002\n\u0000\"\u000e\u0010\u0002\u001a\u00020\u0001X\u0082T¢\u0006\u0002\n\u0000\"\u000e\u0010\u0003\u001a\u00020\u0001X\u0082T¢\u0006\u0002\n\u0000\"\u000e\u0010\u0004\u001a\u00020\u0001X\u0080T¢\u0006\u0002\n\u0000\"\u000e\u0010\u0005\u001a\u00020\u0001X\u0080T¢\u0006\u0002\n\u0000\"\u000e\u0010\u0006\u001a\u00020\u0001X\u0082T¢\u0006\u0002\n\u0000\"\u000e\u0010\u0007\u001a\u00020\u0001X\u0082T¢\u0006\u0002\n\u0000\"\u000e\u0010\b\u001a\u00020\u0001X\u0082T¢\u0006\u0002\n\u0000\"\u000e\u0010\t\u001a\u00020\u0001X\u0082T¢\u0006\u0002\n\u0000\"\u000e\u0010\n\u001a\u00020\u000bX\u0082T¢\u0006\u0002\n\u0000\"\u000e\u0010\f\u001a\u00020\u0001X\u0082T¢\u0006\u0002\n\u0000\"\u000e\u0010\r\u001a\u00020\u0001X\u0082T¢\u0006\u0002\n\u0000\"\u0018\u0010\u000e\u001a\u00020\u000f*\u00020\u00018BX\u0082\u0004¢\u0006\u0006\u001a\u0004\b\u0010\u0010\u0011¨\u00064"}, d2 = {"BIT_FLAG_ENCRYPTED", "", "BIT_FLAG_UNSUPPORTED_MASK", "CENTRAL_FILE_HEADER_SIGNATURE", "COMPRESSION_METHOD_DEFLATED", "COMPRESSION_METHOD_STORED", "END_OF_CENTRAL_DIRECTORY_SIGNATURE", "HEADER_ID_EXTENDED_TIMESTAMP", "HEADER_ID_ZIP64_EXTENDED_INFO", "LOCAL_FILE_HEADER_SIGNATURE", "MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE", "", "ZIP64_EOCD_RECORD_SIGNATURE", "ZIP64_LOCATOR_SIGNATURE", "hex", "", "getHex", "(I)Ljava/lang/String;", "buildIndex", "", "Lokio/Path;", "Lokio/internal/ZipEntry;", "entries", "", "dosDateTimeToEpochMillis", "date", "time", "(II)Ljava/lang/Long;", "openZip", "Lokio/ZipFileSystem;", "zipPath", "fileSystem", "Lokio/FileSystem;", "predicate", "Lkotlin/Function1;", "", "readEntry", "Lokio/BufferedSource;", "readEocdRecord", "Lokio/internal/EocdRecord;", "readExtra", "", "extraSize", "block", "Lkotlin/Function2;", "readLocalHeader", "Lokio/FileMetadata;", "basicMetadata", "readOrSkipLocalHeader", "readZip64EocdRecord", "regularRecord", "skipLocalHeader", "okio"}, k = 2, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes4.dex */
public final class ZipFilesKt {
    private static final int BIT_FLAG_ENCRYPTED = 1;
    private static final int BIT_FLAG_UNSUPPORTED_MASK = 1;
    private static final int CENTRAL_FILE_HEADER_SIGNATURE = 33639248;
    public static final int COMPRESSION_METHOD_DEFLATED = 8;
    public static final int COMPRESSION_METHOD_STORED = 0;
    private static final int END_OF_CENTRAL_DIRECTORY_SIGNATURE = 101010256;
    private static final int HEADER_ID_EXTENDED_TIMESTAMP = 21589;
    private static final int HEADER_ID_ZIP64_EXTENDED_INFO = 1;
    private static final int LOCAL_FILE_HEADER_SIGNATURE = 67324752;
    private static final long MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE = 4294967295L;
    private static final int ZIP64_EOCD_RECORD_SIGNATURE = 101075792;
    private static final int ZIP64_LOCATOR_SIGNATURE = 117853008;

    public static /* synthetic */ ZipFileSystem openZip$default(Path path, FileSystem fileSystem, Function1 function1, int i, Object obj) throws IOException {
        if ((i & 4) != 0) {
            function1 = new Function1<ZipEntry, Boolean>() { // from class: okio.internal.ZipFilesKt.openZip.1
                @Override // kotlin.jvm.functions.Function1
                public final Boolean invoke(ZipEntry it) {
                    Intrinsics.checkNotNullParameter(it, "it");
                    return true;
                }
            };
        }
        return openZip(path, fileSystem, function1);
    }

    /* JADX WARN: Code restructure failed: missing block: B:10:0x0055, code lost:
    
        r13 = r7;
        r15 = readEocdRecord(r0);
     */
    /* JADX WARN: Code restructure failed: missing block: B:11:0x005f, code lost:
    
        r6 = r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:12:0x0064, code lost:
    
        r0 = r6.readUtf8(r15.getCommentByteCount());
     */
    /* JADX WARN: Code restructure failed: missing block: B:14:0x006a, code lost:
    
        r6.close();
        r6 = r13 - 20;
     */
    /* JADX WARN: Code restructure failed: missing block: B:15:0x0076, code lost:
    
        if (r6 <= r18) goto L58;
     */
    /* JADX WARN: Code restructure failed: missing block: B:16:0x0078, code lost:
    
        r10 = okio.Okio.buffer(r0.source(r6));
     */
    /* JADX WARN: Code restructure failed: missing block: B:17:0x0083, code lost:
    
        r0 = r10;
     */
    /* JADX WARN: Code restructure failed: missing block: B:18:0x0091, code lost:
    
        if (r0.readIntLe() != okio.internal.ZipFilesKt.ZIP64_LOCATOR_SIGNATURE) goto L45;
     */
    /* JADX WARN: Code restructure failed: missing block: B:19:0x0093, code lost:
    
        r0 = r0.readIntLe();
        r23 = r0.readLongLe();
        r0 = r0.readIntLe();
     */
    /* JADX WARN: Code restructure failed: missing block: B:21:0x00a9, code lost:
    
        if (r0 != 1) goto L43;
     */
    /* JADX WARN: Code restructure failed: missing block: B:22:0x00ab, code lost:
    
        if (r0 != 0) goto L43;
     */
    /* JADX WARN: Code restructure failed: missing block: B:24:0x00b1, code lost:
    
        r6 = okio.Okio.buffer(r0.source(r23));
     */
    /* JADX WARN: Code restructure failed: missing block: B:25:0x00be, code lost:
    
        r0 = r6;
        r27 = r0.readIntLe();
     */
    /* JADX WARN: Code restructure failed: missing block: B:27:0x00d1, code lost:
    
        if (r27 != okio.internal.ZipFilesKt.ZIP64_EOCD_RECORD_SIGNATURE) goto L34;
     */
    /* JADX WARN: Code restructure failed: missing block: B:28:0x00d3, code lost:
    
        r15 = readZip64EocdRecord(r0, r15);
        r0 = kotlin.Unit.INSTANCE;
     */
    /* JADX WARN: Code restructure failed: missing block: B:30:0x00dc, code lost:
    
        kotlin.io.CloseableKt.closeFinally(r6, null);
     */
    /* JADX WARN: Code restructure failed: missing block: B:32:0x00e0, code lost:
    
        r0 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:33:0x00e1, code lost:
    
        r7 = r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:36:0x0113, code lost:
    
        throw new java.io.IOException("bad zip: expected " + getHex(okio.internal.ZipFilesKt.ZIP64_EOCD_RECORD_SIGNATURE) + " but was " + getHex(r27));
     */
    /* JADX WARN: Code restructure failed: missing block: B:37:0x0114, code lost:
    
        r0 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:38:0x0115, code lost:
    
        r7 = r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:39:0x0118, code lost:
    
        throw r7;
     */
    /* JADX WARN: Code restructure failed: missing block: B:40:0x0119, code lost:
    
        r0 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:42:0x011d, code lost:
    
        throw r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:44:0x0129, code lost:
    
        throw new java.io.IOException("unsupported zip: spanned");
     */
    /* JADX WARN: Code restructure failed: missing block: B:46:0x012c, code lost:
    
        r0 = kotlin.Unit.INSTANCE;
     */
    /* JADX WARN: Code restructure failed: missing block: B:48:0x0130, code lost:
    
        kotlin.io.CloseableKt.closeFinally(r10, null);
     */
    /* JADX WARN: Code restructure failed: missing block: B:50:0x0134, code lost:
    
        r0 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:51:0x0135, code lost:
    
        r6 = r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:52:0x0137, code lost:
    
        r0 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:53:0x0138, code lost:
    
        r6 = r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:54:0x013b, code lost:
    
        throw r6;
     */
    /* JADX WARN: Code restructure failed: missing block: B:55:0x013c, code lost:
    
        r0 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:57:0x0140, code lost:
    
        throw r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:59:0x0143, code lost:
    
        r0 = new java.util.ArrayList();
        r7 = okio.Okio.buffer(r0.source(r15.getCentralDirectoryOffset()));
     */
    /* JADX WARN: Code restructure failed: missing block: B:60:0x015a, code lost:
    
        r0 = r7;
        r16 = 0;
        r25 = r15.getEntryCount();
     */
    /* JADX WARN: Code restructure failed: missing block: B:62:0x0166, code lost:
    
        if (r16 >= r25) goto L125;
     */
    /* JADX WARN: Code restructure failed: missing block: B:63:0x0168, code lost:
    
        r10 = readEntry(r0);
     */
    /* JADX WARN: Code restructure failed: missing block: B:64:0x0176, code lost:
    
        if (r10.getOffset() >= r15.getCentralDirectoryOffset()) goto L124;
     */
    /* JADX WARN: Code restructure failed: missing block: B:66:0x0182, code lost:
    
        if (r3.invoke(r10).booleanValue() == false) goto L68;
     */
    /* JADX WARN: Code restructure failed: missing block: B:67:0x0184, code lost:
    
        r19 = r0;
        r0.add(r10);
     */
    /* JADX WARN: Code restructure failed: missing block: B:68:0x018d, code lost:
    
        r19 = r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:69:0x018f, code lost:
    
        r16 = r16 + 1;
        r0 = r19;
     */
    /* JADX WARN: Code restructure failed: missing block: B:71:0x019f, code lost:
    
        throw new java.io.IOException("bad zip: local file header offset >= central directory offset");
     */
    /* JADX WARN: Code restructure failed: missing block: B:72:0x01a0, code lost:
    
        r0 = kotlin.Unit.INSTANCE;
     */
    /* JADX WARN: Code restructure failed: missing block: B:74:0x01a6, code lost:
    
        kotlin.io.CloseableKt.closeFinally(r7, null);
        r0 = buildIndex(r0);
        r3 = new okio.ZipFileSystem(r31, r32, r0, r0);
     */
    /* JADX WARN: Code restructure failed: missing block: B:75:0x01b2, code lost:
    
        kotlin.io.CloseableKt.closeFinally(r4, null);
     */
    /* JADX WARN: Code restructure failed: missing block: B:76:0x01b6, code lost:
    
        return r3;
     */
    /* JADX WARN: Code restructure failed: missing block: B:77:0x01b7, code lost:
    
        r0 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:83:0x01bf, code lost:
    
        r0 = th;
     */
    /* JADX WARN: Code restructure failed: missing block: B:92:0x01ec, code lost:
    
        r6.close();
     */
    /* JADX WARN: Code restructure failed: missing block: B:93:0x01ef, code lost:
    
        throw r0;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static final okio.ZipFileSystem openZip(okio.Path r31, okio.FileSystem r32, kotlin.jvm.functions.Function1<? super okio.internal.ZipEntry, java.lang.Boolean> r33) throws java.io.IOException {
        /*
            Method dump skipped, instructions count: 535
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: okio.internal.ZipFilesKt.openZip(okio.Path, okio.FileSystem, kotlin.jvm.functions.Function1):okio.ZipFileSystem");
    }

    private static final Map<Path, ZipEntry> buildIndex(List<ZipEntry> list) {
        Path root = Path.Companion.get$default(Path.INSTANCE, "/", false, 1, (Object) null);
        Map result = MapsKt.mutableMapOf(TuplesKt.to(root, new ZipEntry(root, true, null, 0L, 0L, 0L, 0, null, 0L, 508, null)));
        List<ZipEntry> $this$sortedBy$iv = list;
        for (ZipEntry entry : CollectionsKt.sortedWith($this$sortedBy$iv, new Comparator() { // from class: okio.internal.ZipFilesKt$buildIndex$$inlined$sortedBy$1
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.util.Comparator
            public final int compare(T t, T t2) {
                ZipEntry it = (ZipEntry) t;
                ZipEntry it2 = (ZipEntry) t2;
                return ComparisonsKt.compareValues(it.getCanonicalPath(), it2.getCanonicalPath());
            }
        })) {
            ZipEntry replaced = result.put(entry.getCanonicalPath(), entry);
            if (replaced == null) {
                ZipEntry child = entry;
                while (true) {
                    Path parentPath = child.getCanonicalPath().parent();
                    if (parentPath != null) {
                        ZipEntry parentEntry = result.get(parentPath);
                        if (parentEntry != null) {
                            parentEntry.getChildren().add(child.getCanonicalPath());
                            break;
                        }
                        ZipEntry parentEntry2 = new ZipEntry(parentPath, true, null, 0L, 0L, 0L, 0, null, 0L, 508, null);
                        result.put(parentPath, parentEntry2);
                        parentEntry2.getChildren().add(child.getCanonicalPath());
                        child = parentEntry2;
                    }
                }
            }
        }
        return result;
    }

    public static final ZipEntry readEntry(final BufferedSource $this$readEntry) throws IOException {
        Intrinsics.checkNotNullParameter($this$readEntry, "<this>");
        int signature = $this$readEntry.readIntLe();
        if (signature != CENTRAL_FILE_HEADER_SIGNATURE) {
            throw new IOException("bad zip: expected " + getHex(CENTRAL_FILE_HEADER_SIGNATURE) + " but was " + getHex(signature));
        }
        $this$readEntry.skip(4L);
        int bitFlag = $this$readEntry.readShortLe() & UShort.MAX_VALUE;
        if ((bitFlag & 1) != 0) {
            throw new IOException("unsupported zip: general purpose bit flag=" + getHex(bitFlag));
        }
        int compressionMethod = $this$readEntry.readShortLe() & UShort.MAX_VALUE;
        int time = $this$readEntry.readShortLe() & UShort.MAX_VALUE;
        int date = $this$readEntry.readShortLe() & UShort.MAX_VALUE;
        Long lastModifiedAtMillis = dosDateTimeToEpochMillis(date, time);
        long crc = $this$readEntry.readIntLe() & MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE;
        final Ref.LongRef compressedSize = new Ref.LongRef();
        compressedSize.element = $this$readEntry.readIntLe() & MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE;
        final Ref.LongRef size = new Ref.LongRef();
        size.element = $this$readEntry.readIntLe() & MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE;
        int nameSize = $this$readEntry.readShortLe() & UShort.MAX_VALUE;
        int extraSize = $this$readEntry.readShortLe() & UShort.MAX_VALUE;
        int commentByteCount = 65535 & $this$readEntry.readShortLe();
        $this$readEntry.skip(8L);
        final Ref.LongRef offset = new Ref.LongRef();
        offset.element = $this$readEntry.readIntLe() & MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE;
        String name = $this$readEntry.readUtf8(nameSize);
        if (StringsKt.contains$default((CharSequence) name, (char) 0, false, 2, (Object) null)) {
            throw new IOException("bad zip: filename contains 0x00");
        }
        long result = 0;
        if (size.element == MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE) {
            result = 0 + 8;
        }
        if (compressedSize.element == MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE) {
            result += 8;
        }
        if (offset.element == MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE) {
            result += 8;
        }
        final Ref.BooleanRef hasZip64Extra = new Ref.BooleanRef();
        final long requiredZip64ExtraSize = result;
        readExtra($this$readEntry, extraSize, new Function2<Integer, Long, Unit>() { // from class: okio.internal.ZipFilesKt.readEntry.1
            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(2);
            }

            @Override // kotlin.jvm.functions.Function2
            public /* bridge */ /* synthetic */ Unit invoke(Integer num, Long l) throws IOException {
                invoke(num.intValue(), l.longValue());
                return Unit.INSTANCE;
            }

            public final void invoke(int headerId, long dataSize) throws IOException {
                if (headerId == 1) {
                    if (!hasZip64Extra.element) {
                        hasZip64Extra.element = true;
                        if (dataSize < requiredZip64ExtraSize) {
                            throw new IOException("bad zip: zip64 extra too short");
                        }
                        size.element = size.element == ZipFilesKt.MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE ? $this$readEntry.readLongLe() : size.element;
                        compressedSize.element = compressedSize.element == ZipFilesKt.MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE ? $this$readEntry.readLongLe() : 0L;
                        offset.element = offset.element == ZipFilesKt.MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE ? $this$readEntry.readLongLe() : 0L;
                        return;
                    }
                    throw new IOException("bad zip: zip64 extra repeated");
                }
            }
        });
        if (requiredZip64ExtraSize <= 0 || hasZip64Extra.element) {
            String comment = $this$readEntry.readUtf8(commentByteCount);
            Path canonicalPath = Path.Companion.get$default(Path.INSTANCE, "/", false, 1, (Object) null).resolve(name);
            boolean isDirectory = StringsKt.endsWith$default(name, "/", false, 2, (Object) null);
            return new ZipEntry(canonicalPath, isDirectory, comment, crc, compressedSize.element, size.element, compressionMethod, lastModifiedAtMillis, offset.element);
        }
        throw new IOException("bad zip: zip64 extra required but absent");
    }

    private static final EocdRecord readEocdRecord(BufferedSource $this$readEocdRecord) throws IOException {
        int diskNumber = $this$readEocdRecord.readShortLe() & 65535;
        int diskWithCentralDir = $this$readEocdRecord.readShortLe() & 65535;
        long entryCount = $this$readEocdRecord.readShortLe() & UShort.MAX_VALUE;
        long totalEntryCount = $this$readEocdRecord.readShortLe() & UShort.MAX_VALUE;
        if (entryCount != totalEntryCount || diskNumber != 0 || diskWithCentralDir != 0) {
            throw new IOException("unsupported zip: spanned");
        }
        $this$readEocdRecord.skip(4L);
        long centralDirectoryOffset = MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE & $this$readEocdRecord.readIntLe();
        int commentByteCount = $this$readEocdRecord.readShortLe() & UShort.MAX_VALUE;
        return new EocdRecord(entryCount, centralDirectoryOffset, commentByteCount);
    }

    private static final EocdRecord readZip64EocdRecord(BufferedSource $this$readZip64EocdRecord, EocdRecord regularRecord) throws IOException {
        $this$readZip64EocdRecord.skip(12L);
        int diskNumber = $this$readZip64EocdRecord.readIntLe();
        int diskWithCentralDirStart = $this$readZip64EocdRecord.readIntLe();
        long entryCount = $this$readZip64EocdRecord.readLongLe();
        long totalEntryCount = $this$readZip64EocdRecord.readLongLe();
        if (entryCount != totalEntryCount || diskNumber != 0 || diskWithCentralDirStart != 0) {
            throw new IOException("unsupported zip: spanned");
        }
        $this$readZip64EocdRecord.skip(8L);
        long centralDirectoryOffset = $this$readZip64EocdRecord.readLongLe();
        return new EocdRecord(entryCount, centralDirectoryOffset, regularRecord.getCommentByteCount());
    }

    private static final void readExtra(BufferedSource $this$readExtra, int extraSize, Function2<? super Integer, ? super Long, Unit> function2) throws IOException {
        long remaining = extraSize;
        while (remaining != 0) {
            if (remaining < 4) {
                throw new IOException("bad zip: truncated header in extra field");
            }
            int headerId = $this$readExtra.readShortLe() & UShort.MAX_VALUE;
            long dataSize = $this$readExtra.readShortLe() & WebSocketProtocol.PAYLOAD_SHORT_MAX;
            long remaining2 = remaining - 4;
            if (remaining2 < dataSize) {
                throw new IOException("bad zip: truncated value in extra field");
            }
            $this$readExtra.require(dataSize);
            long sizeBefore = $this$readExtra.getBuffer().size();
            function2.invoke(Integer.valueOf(headerId), Long.valueOf(dataSize));
            long fieldRemaining = ($this$readExtra.getBuffer().size() + dataSize) - sizeBefore;
            if (fieldRemaining < 0) {
                throw new IOException("unsupported zip: too many bytes processed for " + headerId);
            }
            if (fieldRemaining > 0) {
                $this$readExtra.getBuffer().skip(fieldRemaining);
            }
            remaining = remaining2 - dataSize;
        }
    }

    public static final void skipLocalHeader(BufferedSource $this$skipLocalHeader) throws IOException {
        Intrinsics.checkNotNullParameter($this$skipLocalHeader, "<this>");
        readOrSkipLocalHeader($this$skipLocalHeader, null);
    }

    public static final FileMetadata readLocalHeader(BufferedSource $this$readLocalHeader, FileMetadata basicMetadata) throws IOException {
        Intrinsics.checkNotNullParameter($this$readLocalHeader, "<this>");
        Intrinsics.checkNotNullParameter(basicMetadata, "basicMetadata");
        FileMetadata orSkipLocalHeader = readOrSkipLocalHeader($this$readLocalHeader, basicMetadata);
        Intrinsics.checkNotNull(orSkipLocalHeader);
        return orSkipLocalHeader;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static final FileMetadata readOrSkipLocalHeader(final BufferedSource bufferedSource, FileMetadata fileMetadata) throws IOException {
        final Ref.ObjectRef objectRef = new Ref.ObjectRef();
        objectRef.element = fileMetadata != null ? fileMetadata.getLastModifiedAtMillis() : 0;
        final Ref.ObjectRef objectRef2 = new Ref.ObjectRef();
        final Ref.ObjectRef objectRef3 = new Ref.ObjectRef();
        int intLe = bufferedSource.readIntLe();
        if (intLe == LOCAL_FILE_HEADER_SIGNATURE) {
            bufferedSource.skip(2L);
            int shortLe = bufferedSource.readShortLe() & UShort.MAX_VALUE;
            if ((shortLe & 1) == 0) {
                bufferedSource.skip(18L);
                long shortLe2 = bufferedSource.readShortLe() & WebSocketProtocol.PAYLOAD_SHORT_MAX;
                int shortLe3 = 65535 & bufferedSource.readShortLe();
                bufferedSource.skip(shortLe2);
                if (fileMetadata == null) {
                    bufferedSource.skip(shortLe3);
                    return null;
                }
                readExtra(bufferedSource, shortLe3, new Function2<Integer, Long, Unit>() { // from class: okio.internal.ZipFilesKt.readOrSkipLocalHeader.1
                    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                    {
                        super(2);
                    }

                    @Override // kotlin.jvm.functions.Function2
                    public /* bridge */ /* synthetic */ Unit invoke(Integer num, Long l) throws IOException {
                        invoke(num.intValue(), l.longValue());
                        return Unit.INSTANCE;
                    }

                    /* JADX WARN: Type inference failed for: r4v12, types: [T, java.lang.Long] */
                    /* JADX WARN: Type inference failed for: r4v6, types: [T, java.lang.Long] */
                    /* JADX WARN: Type inference failed for: r4v9, types: [T, java.lang.Long] */
                    public final void invoke(int headerId, long dataSize) throws IOException {
                        if (headerId == ZipFilesKt.HEADER_ID_EXTENDED_TIMESTAMP) {
                            if (dataSize < 1) {
                                throw new IOException("bad zip: extended timestamp extra too short");
                            }
                            int flags = bufferedSource.readByte() & 255;
                            boolean hasLastModifiedAtMillis = (flags & 1) == 1;
                            boolean hasLastAccessedAtMillis = (flags & 2) == 2;
                            boolean hasCreatedAtMillis = (flags & 4) == 4;
                            BufferedSource bufferedSource2 = bufferedSource;
                            long result = hasLastModifiedAtMillis ? 1 + 4 : 1L;
                            if (hasLastAccessedAtMillis) {
                                result += 4;
                            }
                            if (hasCreatedAtMillis) {
                                result += 4;
                            }
                            if (dataSize < result) {
                                throw new IOException("bad zip: extended timestamp extra too short");
                            }
                            if (hasLastModifiedAtMillis) {
                                objectRef.element = Long.valueOf(bufferedSource.readIntLe() * 1000);
                            }
                            if (hasLastAccessedAtMillis) {
                                objectRef2.element = Long.valueOf(bufferedSource.readIntLe() * 1000);
                            }
                            if (hasCreatedAtMillis) {
                                objectRef3.element = Long.valueOf(bufferedSource.readIntLe() * 1000);
                            }
                        }
                    }
                });
                return new FileMetadata(fileMetadata.getIsRegularFile(), fileMetadata.getIsDirectory(), null, fileMetadata.getSize(), (Long) objectRef3.element, (Long) objectRef.element, (Long) objectRef2.element, null, 128, null);
            }
            throw new IOException("unsupported zip: general purpose bit flag=" + getHex(shortLe));
        }
        throw new IOException("bad zip: expected " + getHex(LOCAL_FILE_HEADER_SIGNATURE) + " but was " + getHex(intLe));
    }

    private static final Long dosDateTimeToEpochMillis(int date, int time) {
        if (time == -1) {
            return null;
        }
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(14, 0);
        int year = ((date >> 9) & 127) + 1980;
        int month = (date >> 5) & 15;
        int day = date & 31;
        int hour = (time >> 11) & 31;
        int minute = (time >> 5) & 63;
        int second = (time & 31) << 1;
        cal.set(year, month - 1, day, hour, minute, second);
        return Long.valueOf(cal.getTime().getTime());
    }

    private static final String getHex(int $this$hex) {
        StringBuilder sbAppend = new StringBuilder().append("0x");
        String string = Integer.toString($this$hex, CharsKt.checkRadix(16));
        Intrinsics.checkNotNullExpressionValue(string, "toString(this, checkRadix(radix))");
        return sbAppend.append(string).toString();
    }
}
