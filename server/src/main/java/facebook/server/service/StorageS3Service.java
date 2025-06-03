package facebook.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.Map;

@Service
public class StorageS3Service {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StorageS3Service.class);

    @Value("${filebase.endpoint}")
    private String filebaseEndpoint;

    @Value("${filebase.bucket.name}")
    private String bucketName;

    @Autowired
    private S3Client s3Client;

    public String uploadFile(MultipartFile file, String uploadName) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty.");
        }

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uploadName)
                .build();

        String cid = "";
        try {
            PutObjectResponse putResp = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            logger.info("File uploaded successfully.");

            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uploadName)
                    .build();

            HeadObjectResponse headResponse = s3Client.headObject(headRequest);
            Map<String, String> userMetadata = headResponse.metadata();

            cid = userMetadata.get("cid");

            if (cid == null || cid.isEmpty()) {
                logger.warn("CID not found in metadata. Available metadata:");
                userMetadata.forEach((key, value) -> logger.debug("{} : {}", key, value));
            } else {
                logger.info("File Base CID: {}", cid);
            }
        } catch (IOException e) {
            logger.error("AWS error occurred: {}", e.getMessage(), e);
        }

        return "https://steep-cyan-anaconda.myfilebase.com/ipfs/" + cid;
    }
    public byte[] getImage(String imageName) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(imageName)
                .build();

        ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getObjectRequest);
        return s3ObjectStream.readAllBytes();
    }

    public void deleteFile(String fileName) {
        s3Client.deleteObject(builder -> builder.bucket(bucketName).key(fileName));
    }
}
