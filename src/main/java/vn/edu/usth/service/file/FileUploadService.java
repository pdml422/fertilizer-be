package vn.edu.usth.service.file;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import vn.edu.usth.model.Image;
import vn.edu.usth.repository.ImageRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
public class FileUploadService {
    @ConfigProperty(name = "upload.directory")
    String UPLOAD_DIR;

    private final ImageRepository imageRepository;
    @Inject
    public FileUploadService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Transactional
    public String uploadFile(MultipartFormDataInput input, int userId) {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<String> fileNames = new ArrayList<>();
        List<InputPart> headerParts = uploadForm.get("hyper-header");
        List<InputPart> imageParts = uploadForm.get("hyper-image");
        String fileName = null;

        for (InputPart inputPart : headerParts) {
            try {
                MultivaluedMap<String, String> header = inputPart.getHeaders();
                fileName = getFileName(header);
                fileNames.add(fileName);
                InputStream inputStream = inputPart.getBody(InputStream.class, null);
                writeFile(inputStream,fileName,userId);

                String path = "src/main/resources/Image/" + userId + "/Hyper_spectral/" + fileName;
                Image image = new Image();
                image.setPath(path);
                image.setUserId(userId);
                image.setType("hyper-header");
                imageRepository.persist(image);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (InputPart inputPart : imageParts) {
            try {
                MultivaluedMap<String, String> header = inputPart.getHeaders();
                fileName = getFileName(header);
                fileNames.add(fileName);
                InputStream inputStream = inputPart.getBody(InputStream.class, null);
                writeFile(inputStream,fileName,userId);

                String path = "src/main/resources/Image/" + userId + "/Hyper_spectral/" + fileName;
                Image image = new Image();
                image.setPath(path);
                image.setUserId(userId);
                image.setType("hyper-image");
                imageRepository.persist(image);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "Files Successfully Uploaded";
    }

    private void writeFile(InputStream inputStream,String fileName, int userId) {
        File customDir = new File(UPLOAD_DIR + userId + "/Hyper_spectral");
        fileName = customDir.getAbsolutePath() + File.separator + fileName;

        try (FileOutputStream fos = new FileOutputStream(fileName);
             FileChannel channel = fos.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            byte[] bytes = new byte[1024];
            int length;

            while ((length = inputStream.read(bytes)) != -1) {
                buffer.put(bytes, 0, length);
                if (!buffer.hasRemaining()) {
                    buffer.flip();
                    channel.write(buffer);
                    buffer.clear();
                }
            }

            if (buffer.position() > 0) {
                buffer.flip();
                channel.write(buffer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "";
    }

}