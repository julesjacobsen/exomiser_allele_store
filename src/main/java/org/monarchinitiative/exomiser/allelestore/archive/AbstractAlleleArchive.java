package org.monarchinitiative.exomiser.allelestore.archive;

import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AbstractAlleleArchive implements AlleleArchive {

    private final Path archivePath;
    private final String archiveFormat;
    private final String dataFileFormat;

    /**
     * @param archivePath    path of the original compressed archive file
     * @param archiveFormat  format of the original compressed archive file - tgz, gz or zip
     * @param dataFileFormat extension of the uncompressed data file inside the archive file - usually vcf, but dbNSFP uses .chr[1-22,X,Y,M]
     */
    public AbstractAlleleArchive(Path archivePath, String archiveFormat, String dataFileFormat) {
        this.archivePath = archivePath;
        this.archiveFormat = archiveFormat;
        this.dataFileFormat = dataFileFormat;
    }

    public Path getPath() {
        return archivePath;
    }

    public String getArchiveFileFormat() {
        return archiveFormat;
    }

    @Override
    public String getDataFileFormat() {
        return dataFileFormat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractAlleleArchive that = (AbstractAlleleArchive) o;
        return Objects.equals(archivePath, that.archivePath) &&
                Objects.equals(archiveFormat, that.archiveFormat) &&
                Objects.equals(dataFileFormat, that.dataFileFormat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(archivePath, archiveFormat, dataFileFormat);
    }

    @Override
    public String toString() {
        return "AbstractArchiveFile{" +
                "archivePath=" + archivePath +
                ", archiveFormat='" + archiveFormat + '\'' +
                ", dataFileFormat='" + dataFileFormat + '\'' +
                '}';
    }
}
