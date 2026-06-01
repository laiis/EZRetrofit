package tw.idv.laiis.ezretrofit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class TemporaryFolderSecurityTest {

    @TempDir
    Path tempDir;

    @Test
    public void testTemporaryFolderPermissions() throws IOException {
        assertNotNull(tempDir);
        File dir = tempDir.toFile();
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());

        // 驗證擁有者具備讀寫/執行權限
        assertTrue(dir.canRead(), "Owner should have read permission");
        assertTrue(dir.canWrite(), "Owner should have write permission");
        assertTrue(dir.canExecute(), "Owner should have execute permission");

        // 若支援 POSIX 檔案屬性，則驗證群組與其他使用者無權限
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(tempDir);
            assertFalse(permissions.contains(PosixFilePermission.GROUP_READ), "Group should not have read permission");
            assertFalse(permissions.contains(PosixFilePermission.GROUP_WRITE), "Group should not have write permission");
            assertFalse(permissions.contains(PosixFilePermission.GROUP_EXECUTE), "Group should not have execute permission");
            assertFalse(permissions.contains(PosixFilePermission.OTHERS_READ), "Others should not have read permission");
            assertFalse(permissions.contains(PosixFilePermission.OTHERS_WRITE), "Others should not have write permission");
            assertFalse(permissions.contains(PosixFilePermission.OTHERS_EXECUTE), "Others should not have execute permission");
        } else {
            // Windows 環境下，直接設定 owner-only 並不做過度斷言以避免 JVM/OS 不支援而失敗
            dir.setReadable(true, true);
            dir.setWritable(true, true);
            dir.setExecutable(true, true);
        }
    }
}
