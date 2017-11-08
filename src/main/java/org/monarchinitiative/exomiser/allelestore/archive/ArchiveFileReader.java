package org.monarchinitiative.exomiser.allelestore.archive;

import org.apache.commons.vfs2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ArchiveFileReader {

    private static final Logger logger = LoggerFactory.getLogger(ArchiveFileReader.class);

    private final Path archiveFileName;
    private final String archiveFormat;
    private final String dataFileFormat;

    public ArchiveFileReader(AlleleArchive alleleArchive) {
        this.archiveFileName = alleleArchive.getPath();
        this.archiveFormat = alleleArchive.getArchiveFileFormat();
        this.dataFileFormat = alleleArchive.getDataFileFormat();
    }

    public List<FileObject> getFileObjects() {
        List<FileObject> archiveFileInputStreams = new ArrayList<>();
        try {
            FileSystemManager fileSystemManager = VFS.getManager();
            FileObject archive = fileSystemManager.resolveFile(archiveFormat + ":file://" + archiveFileName.toAbsolutePath());
            for (FileObject fileObject : archive.getChildren()) {
                if (fileObject.getName().getExtension().startsWith(dataFileFormat)) {
                    archiveFileInputStreams.add(fileObject);
                }
            }
        } catch (FileSystemException e) {
            logger.error("{}", e);
        }
        return archiveFileInputStreams;
    }

    public InputStream readFileObject(FileObject fileObject) throws IOException {
        logger.info("Reading archive file {}", fileObject.getName());
        FileContent fileContent = fileObject.getContent();
        return fileContent.getInputStream();
    }

}
