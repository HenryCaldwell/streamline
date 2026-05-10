package info.henrycaldwell.aggregator.stage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import info.henrycaldwell.aggregator.core.MediaRef;
import info.henrycaldwell.aggregator.error.ComponentException;
import info.henrycaldwell.aggregator.error.SpecException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

public class CloudflareR2StagerTest {

  @TempDir
  Path tempDir;

  @Nested
  class Constructor {

    @Test
    void acceptsMinimalConfig() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);

      assertDoesNotThrow(() -> new CloudflareR2Stager(config));
    }

    @Test
    void acceptsConfiguredRegion() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          region = us-east-1
          """);

      assertDoesNotThrow(() -> new CloudflareR2Stager(config));
    }

    @Test
    void acceptsConfiguredEndpoint() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          endpoint = "https://custom.endpoint.com"
          """);

      assertDoesNotThrow(() -> new CloudflareR2Stager(config));
    }

    @Test
    void throwsOnMissingAccountId() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);

      SpecException exception = assertThrows(SpecException.class, () -> new CloudflareR2Stager(config));

      assertTrue(exception.getMessage().contains("Missing required key"));
      assertTrue(exception.getMessage().contains("key=accountId"));
    }

    @Test
    void throwsOnWrongTypeForAccountId() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = [account-1]
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);

      SpecException exception = assertThrows(SpecException.class, () -> new CloudflareR2Stager(config));

      assertTrue(exception.getMessage().contains("Incorrect key type (expected string)"));
      assertTrue(exception.getMessage().contains("key=accountId"));
    }

    @Test
    void throwsOnMissingAccessKey() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);

      SpecException exception = assertThrows(SpecException.class, () -> new CloudflareR2Stager(config));

      assertTrue(exception.getMessage().contains("Missing required key"));
      assertTrue(exception.getMessage().contains("key=accessKey"));
    }

    @Test
    void throwsOnWrongTypeForAccessKey() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = [key-1]
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);

      SpecException exception = assertThrows(SpecException.class, () -> new CloudflareR2Stager(config));

      assertTrue(exception.getMessage().contains("Incorrect key type (expected string)"));
      assertTrue(exception.getMessage().contains("key=accessKey"));
    }

    @Test
    void throwsOnMissingSecretKey() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);

      SpecException exception = assertThrows(SpecException.class, () -> new CloudflareR2Stager(config));

      assertTrue(exception.getMessage().contains("Missing required key"));
      assertTrue(exception.getMessage().contains("key=secretKey"));
    }

    @Test
    void throwsOnWrongTypeForSecretKey() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = [secret-1]
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);

      SpecException exception = assertThrows(SpecException.class, () -> new CloudflareR2Stager(config));

      assertTrue(exception.getMessage().contains("Incorrect key type (expected string)"));
      assertTrue(exception.getMessage().contains("key=secretKey"));
    }

    @Test
    void throwsOnMissingBucket() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          publicUrl = "https://cdn.example.com"
          """);

      SpecException exception = assertThrows(SpecException.class, () -> new CloudflareR2Stager(config));

      assertTrue(exception.getMessage().contains("Missing required key"));
      assertTrue(exception.getMessage().contains("key=bucket"));
    }

    @Test
    void throwsOnWrongTypeForBucket() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = [my-bucket]
          publicUrl = "https://cdn.example.com"
          """);

      SpecException exception = assertThrows(SpecException.class, () -> new CloudflareR2Stager(config));

      assertTrue(exception.getMessage().contains("Incorrect key type (expected string)"));
      assertTrue(exception.getMessage().contains("key=bucket"));
    }

    @Test
    void throwsOnMissingPublicUrl() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          """);

      SpecException exception = assertThrows(SpecException.class, () -> new CloudflareR2Stager(config));

      assertTrue(exception.getMessage().contains("Missing required key"));
      assertTrue(exception.getMessage().contains("key=publicUrl"));
    }

    @Test
    void throwsOnWrongTypeForPublicUrl() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = ["https://cdn.example.com"]
          """);

      SpecException exception = assertThrows(SpecException.class, () -> new CloudflareR2Stager(config));

      assertTrue(exception.getMessage().contains("Incorrect key type (expected string)"));
      assertTrue(exception.getMessage().contains("key=publicUrl"));
    }

    @Test
    void throwsOnWrongTypeForRegion() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          region = [us-east-1]
          """);

      SpecException exception = assertThrows(SpecException.class, () -> new CloudflareR2Stager(config));

      assertTrue(exception.getMessage().contains("Incorrect key type (expected string)"));
      assertTrue(exception.getMessage().contains("key=region"));
    }

    @Test
    void throwsOnWrongTypeForEndpoint() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          endpoint = ["https://custom.endpoint.com"]
          """);

      SpecException exception = assertThrows(SpecException.class, () -> new CloudflareR2Stager(config));

      assertTrue(exception.getMessage().contains("Incorrect key type (expected string)"));
      assertTrue(exception.getMessage().contains("key=endpoint"));
    }

    @Test
    void throwsOnUnknownKey() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          extra = value
          """);

      SpecException exception = assertThrows(SpecException.class, () -> new CloudflareR2Stager(config));

      assertTrue(exception.getMessage().contains("Unknown configuration key"));
      assertTrue(exception.getMessage().contains("key=extra"));
    }
  }

  @Nested
  class Start {

    @Test
    void allowsApplyAfterStart() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      CloudflareR2Stager stager = new CloudflareR2Stager(config);
      stager.start();

      MediaRef media = new MediaRef("clip-1", null, null, "Title", "Broadcaster", "en", null);
      ComponentException exception = assertThrows(ComponentException.class, () -> stager.apply(media));

      assertTrue(exception.getMessage().contains("Input file missing or not a regular file"));
      stager.stop();
    }

    @Test
    void isIdempotentWhenStarted() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      CloudflareR2Stager stager = new CloudflareR2Stager(config);

      assertDoesNotThrow(() -> {
        stager.start();
        stager.start();
      });
      stager.stop();
    }
  }

  @Nested
  class Stop {

    @Test
    void stopsStartedStager() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      CloudflareR2Stager stager = new CloudflareR2Stager(config);
      stager.start();
      stager.stop();

      MediaRef media = new MediaRef("clip-1", null, null, "Title", "Broadcaster", "en", null);
      ComponentException exception = assertThrows(ComponentException.class, () -> stager.apply(media));

      assertTrue(exception.getMessage().contains("Stager not started"));
    }

    @Test
    void isIdempotentWhenNotStarted() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      CloudflareR2Stager stager = new CloudflareR2Stager(config);

      assertDoesNotThrow(() -> stager.stop());
    }
  }

  @Nested
  class Apply {

    @Test
    void returnsMediaRefWithPublicUriOnSuccess() throws IOException {
      Path source = tempDir.resolve("clip.mp4");
      Files.writeString(source, "data");

      MediaRef media = new MediaRef("clip-1", source, null, "Title", "Broadcaster", "en", null);
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      S3Operations operations = new S3Operations() {
        @Override
        public void putObject(PutObjectRequest request, RequestBody body) {
        }

        @Override
        public void deleteObject(DeleteObjectRequest request) {
        }

        @Override
        public void deleteObjects(DeleteObjectsRequest request) {
        }

        @Override
        public ListObjectsV2Response listObjectsV2(ListObjectsV2Request request) {
          return ListObjectsV2Response.builder().isTruncated(false).build();
        }
      };
      CloudflareR2Stager stager = new CloudflareR2Stager(config, operations);

      MediaRef result = assertDoesNotThrow(() -> stager.apply(media));

      assertEquals(URI.create("https://cdn.example.com/clip.mp4"), result.uri());
      assertNull(result.file());
    }

    @Test
    void throwsWhenNotStarted() {
      MediaRef media = new MediaRef("clip-1", null, null, "Title", "Broadcaster", "en", null);
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      CloudflareR2Stager stager = new CloudflareR2Stager(config);

      ComponentException exception = assertThrows(ComponentException.class, () -> stager.apply(media));

      assertTrue(exception.getMessage().contains("Stager not started"));
    }

    @Test
    void throwsWhenSourceIsNull() {
      MediaRef media = new MediaRef("clip-1", null, null, "Title", "Broadcaster", "en", null);
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      S3Operations operations = new S3Operations() {
        @Override
        public void putObject(PutObjectRequest request, RequestBody body) {
        }

        @Override
        public void deleteObject(DeleteObjectRequest request) {
        }

        @Override
        public void deleteObjects(DeleteObjectsRequest request) {
        }

        @Override
        public ListObjectsV2Response listObjectsV2(ListObjectsV2Request request) {
          return ListObjectsV2Response.builder().isTruncated(false).build();
        }
      };
      CloudflareR2Stager stager = new CloudflareR2Stager(config, operations);

      ComponentException exception = assertThrows(ComponentException.class, () -> stager.apply(media));

      assertTrue(exception.getMessage().contains("Input file missing or not a regular file"));
    }

    @Test
    void throwsWhenSourceIsMissing() {
      Path source = tempDir.resolve("nonexistent.mp4");

      MediaRef media = new MediaRef("clip-1", source, null, "Title", "Broadcaster", "en", null);
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      S3Operations operations = new S3Operations() {
        @Override
        public void putObject(PutObjectRequest request, RequestBody body) {
        }

        @Override
        public void deleteObject(DeleteObjectRequest request) {
        }

        @Override
        public void deleteObjects(DeleteObjectsRequest request) {
        }

        @Override
        public ListObjectsV2Response listObjectsV2(ListObjectsV2Request request) {
          return ListObjectsV2Response.builder().isTruncated(false).build();
        }
      };
      CloudflareR2Stager stager = new CloudflareR2Stager(config, operations);

      ComponentException exception = assertThrows(ComponentException.class, () -> stager.apply(media));

      assertTrue(exception.getMessage().contains("Input file missing or not a regular file"));
      assertTrue(exception.getMessage().contains("sourcePath="));
    }

    @Test
    void throwsWhenSourceIsNotARegularFile() throws IOException {
      Path source = tempDir.resolve("source.mp4");
      Files.createDirectory(source);

      MediaRef media = new MediaRef("clip-1", source, null, "Title", "Broadcaster", "en", null);
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      S3Operations operations = new S3Operations() {
        @Override
        public void putObject(PutObjectRequest request, RequestBody body) {
        }

        @Override
        public void deleteObject(DeleteObjectRequest request) {
        }

        @Override
        public void deleteObjects(DeleteObjectsRequest request) {
        }

        @Override
        public ListObjectsV2Response listObjectsV2(ListObjectsV2Request request) {
          return ListObjectsV2Response.builder().isTruncated(false).build();
        }
      };
      CloudflareR2Stager stager = new CloudflareR2Stager(config, operations);

      ComponentException exception = assertThrows(ComponentException.class, () -> stager.apply(media));

      assertTrue(exception.getMessage().contains("Input file missing or not a regular file"));
      assertTrue(exception.getMessage().contains("sourcePath=" + source));
    }

    @Test
    void throwsWhenUploadFails() throws IOException {
      Path source = tempDir.resolve("clip.mp4");
      Files.writeString(source, "data");

      MediaRef media = new MediaRef("clip-1", source, null, "Title", "Broadcaster", "en", null);
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      S3Operations operations = new S3Operations() {
        @Override
        public void putObject(PutObjectRequest request, RequestBody body) {
          throw new RuntimeException("Upload failed");
        }

        @Override
        public void deleteObject(DeleteObjectRequest request) {
        }

        @Override
        public void deleteObjects(DeleteObjectsRequest request) {
        }

        @Override
        public ListObjectsV2Response listObjectsV2(ListObjectsV2Request request) {
          return ListObjectsV2Response.builder().isTruncated(false).build();
        }
      };
      CloudflareR2Stager stager = new CloudflareR2Stager(config, operations);

      ComponentException exception = assertThrows(ComponentException.class, () -> stager.apply(media));

      assertTrue(exception.getMessage().contains("Failed to upload object to R2"));
    }
  }

  @Nested
  class Clean {

    @Test
    void deletesObjectOnSuccess() {
      boolean[] deleted = { false };

      MediaRef media = new MediaRef("clip-1", null, URI.create("https://cdn.example.com/clip.mp4"), "Title",
          "Broadcaster", "en", null);
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      S3Operations operations = new S3Operations() {
        @Override
        public void putObject(PutObjectRequest request, RequestBody body) {
        }

        @Override
        public void deleteObject(DeleteObjectRequest request) {
          deleted[0] = true;
        }

        @Override
        public void deleteObjects(DeleteObjectsRequest request) {
        }

        @Override
        public ListObjectsV2Response listObjectsV2(ListObjectsV2Request request) {
          return ListObjectsV2Response.builder().isTruncated(false).build();
        }
      };
      CloudflareR2Stager stager = new CloudflareR2Stager(config, operations);

      assertDoesNotThrow(() -> stager.clean(media));

      assertTrue(deleted[0]);
    }

    @Test
    void throwsWhenUriIsNull() {
      MediaRef media = new MediaRef("clip-1", null, null, "Title", "Broadcaster", "en", null);
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      S3Operations operations = new S3Operations() {
        @Override
        public void putObject(PutObjectRequest request, RequestBody body) {
        }

        @Override
        public void deleteObject(DeleteObjectRequest request) {
        }

        @Override
        public void deleteObjects(DeleteObjectsRequest request) {
        }

        @Override
        public ListObjectsV2Response listObjectsV2(ListObjectsV2Request request) {
          return ListObjectsV2Response.builder().isTruncated(false).build();
        }
      };
      CloudflareR2Stager stager = new CloudflareR2Stager(config, operations);

      ComponentException exception = assertThrows(ComponentException.class, () -> stager.clean(media));

      assertTrue(exception.getMessage().contains("Staged media URI missing"));
    }

    @Test
    void throwsWhenUriPathIsBlank() {
      MediaRef media = new MediaRef("clip-1", null, URI.create("https://cdn.example.com"), "Title", "Broadcaster",
          "en", null);
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      S3Operations operations = new S3Operations() {
        @Override
        public void putObject(PutObjectRequest request, RequestBody body) {
        }

        @Override
        public void deleteObject(DeleteObjectRequest request) {
        }

        @Override
        public void deleteObjects(DeleteObjectsRequest request) {
        }

        @Override
        public ListObjectsV2Response listObjectsV2(ListObjectsV2Request request) {
          return ListObjectsV2Response.builder().isTruncated(false).build();
        }
      };
      CloudflareR2Stager stager = new CloudflareR2Stager(config, operations);

      ComponentException exception = assertThrows(ComponentException.class, () -> stager.clean(media));

      assertTrue(exception.getMessage().contains("Staged media URI path missing"));
    }

    @Test
    void throwsWhenUriObjectKeyIsEmpty() {
      MediaRef media = new MediaRef("clip-1", null, URI.create("https://cdn.example.com/"), "Title", "Broadcaster",
          "en", null);
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      S3Operations operations = new S3Operations() {
        @Override
        public void putObject(PutObjectRequest request, RequestBody body) {
        }

        @Override
        public void deleteObject(DeleteObjectRequest request) {
        }

        @Override
        public void deleteObjects(DeleteObjectsRequest request) {
        }

        @Override
        public ListObjectsV2Response listObjectsV2(ListObjectsV2Request request) {
          return ListObjectsV2Response.builder().isTruncated(false).build();
        }
      };
      CloudflareR2Stager stager = new CloudflareR2Stager(config, operations);

      ComponentException exception = assertThrows(ComponentException.class, () -> stager.clean(media));

      assertTrue(exception.getMessage().contains("Staged media URI object key empty"));
    }

    @Test
    void throwsWhenNotStarted() {
      MediaRef media = new MediaRef("clip-1", null, URI.create("https://cdn.example.com/clip.mp4"), "Title",
          "Broadcaster", "en", null);
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      CloudflareR2Stager stager = new CloudflareR2Stager(config);

      ComponentException exception = assertThrows(ComponentException.class, () -> stager.clean(media));

      assertTrue(exception.getMessage().contains("Stager not started"));
    }

    @Test
    void throwsWhenDeleteFails() {
      MediaRef media = new MediaRef("clip-1", null, URI.create("https://cdn.example.com/clip.mp4"), "Title",
          "Broadcaster", "en", null);
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      S3Operations operations = new S3Operations() {
        @Override
        public void putObject(PutObjectRequest request, RequestBody body) {
        }

        @Override
        public void deleteObject(DeleteObjectRequest request) {
          throw new RuntimeException("Delete failed");
        }

        @Override
        public void deleteObjects(DeleteObjectsRequest request) {
        }

        @Override
        public ListObjectsV2Response listObjectsV2(ListObjectsV2Request request) {
          return ListObjectsV2Response.builder().isTruncated(false).build();
        }
      };
      CloudflareR2Stager stager = new CloudflareR2Stager(config, operations);

      ComponentException exception = assertThrows(ComponentException.class, () -> stager.clean(media));

      assertTrue(exception.getMessage().contains("Failed to delete object from R2"));
    }
  }

  @Nested
  class Purge {

    @Test
    void deletesAllObjectsOnSuccess() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      List<String> deletedKeys = new ArrayList<>();
      S3Operations operations = new S3Operations() {
        @Override
        public void putObject(PutObjectRequest request, RequestBody body) {
        }

        @Override
        public void deleteObject(DeleteObjectRequest request) {
        }

        @Override
        public void deleteObjects(DeleteObjectsRequest request) {
          request.delete().objects().forEach(o -> deletedKeys.add(o.key()));
        }

        @Override
        public ListObjectsV2Response listObjectsV2(ListObjectsV2Request request) {
          return ListObjectsV2Response.builder()
              .contents(
                  S3Object.builder().key("clip-1.mp4").build(),
                  S3Object.builder().key("clip-2.mp4").build())
              .isTruncated(false)
              .build();
        }
      };
      CloudflareR2Stager stager = new CloudflareR2Stager(config, operations);

      assertDoesNotThrow(() -> stager.purge());

      assertEquals(2, deletedKeys.size());
      assertTrue(deletedKeys.contains("clip-1.mp4"));
      assertTrue(deletedKeys.contains("clip-2.mp4"));
    }

    @Test
    void deletesAllPagesOnSuccess() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      List<String> deletedKeys = new ArrayList<>();
      S3Operations operations = new S3Operations() {
        @Override
        public void putObject(PutObjectRequest request, RequestBody body) {
        }

        @Override
        public void deleteObject(DeleteObjectRequest request) {
        }

        @Override
        public void deleteObjects(DeleteObjectsRequest request) {
          request.delete().objects().forEach(o -> deletedKeys.add(o.key()));
        }

        @Override
        public ListObjectsV2Response listObjectsV2(ListObjectsV2Request request) {
          if (request.continuationToken() == null) {
            return ListObjectsV2Response.builder()
                .contents(S3Object.builder().key("clip-1.mp4").build())
                .isTruncated(true)
                .nextContinuationToken("token-1")
                .build();
          }

          return ListObjectsV2Response.builder()
              .contents(S3Object.builder().key("clip-2.mp4").build())
              .isTruncated(false)
              .build();
        }
      };
      CloudflareR2Stager stager = new CloudflareR2Stager(config, operations);

      assertDoesNotThrow(() -> stager.purge());

      assertEquals(2, deletedKeys.size());
      assertTrue(deletedKeys.contains("clip-1.mp4"));
      assertTrue(deletedKeys.contains("clip-2.mp4"));
    }

    @Test
    void doesNothingWhenBucketIsEmpty() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      boolean[] deleteObjectsCalled = { false };
      S3Operations operations = new S3Operations() {
        @Override
        public void putObject(PutObjectRequest request, RequestBody body) {
        }

        @Override
        public void deleteObject(DeleteObjectRequest request) {
        }

        @Override
        public void deleteObjects(DeleteObjectsRequest request) {
          deleteObjectsCalled[0] = true;
        }

        @Override
        public ListObjectsV2Response listObjectsV2(ListObjectsV2Request request) {
          return ListObjectsV2Response.builder().isTruncated(false).build();
        }
      };
      CloudflareR2Stager stager = new CloudflareR2Stager(config, operations);

      assertDoesNotThrow(() -> stager.purge());
      assertFalse(deleteObjectsCalled[0]);
    }

    @Test
    void throwsWhenNotStarted() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      CloudflareR2Stager stager = new CloudflareR2Stager(config);

      ComponentException exception = assertThrows(ComponentException.class, () -> stager.purge());

      assertTrue(exception.getMessage().contains("Stager not started"));
    }

    @Test
    void throwsWhenListFails() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      S3Operations operations = new S3Operations() {
        @Override
        public void putObject(PutObjectRequest request, RequestBody body) {
        }

        @Override
        public void deleteObject(DeleteObjectRequest request) {
        }

        @Override
        public void deleteObjects(DeleteObjectsRequest request) {
        }

        @Override
        public ListObjectsV2Response listObjectsV2(ListObjectsV2Request request) {
          throw new RuntimeException("List failed");
        }
      };
      CloudflareR2Stager stager = new CloudflareR2Stager(config, operations);

      ComponentException exception = assertThrows(ComponentException.class, () -> stager.purge());

      assertTrue(exception.getMessage().contains("Failed to list objects in R2"));
    }

    @Test
    void throwsWhenDeleteFails() {
      Config config = ConfigFactory.parseString("""
          name = stager
          type = cloudflare-r2
          accountId = account-1
          accessKey = key-1
          secretKey = secret-1
          bucket = my-bucket
          publicUrl = "https://cdn.example.com"
          """);
      S3Operations operations = new S3Operations() {
        @Override
        public void putObject(PutObjectRequest request, RequestBody body) {
        }

        @Override
        public void deleteObject(DeleteObjectRequest request) {
        }

        @Override
        public void deleteObjects(DeleteObjectsRequest request) {
          throw new RuntimeException("Delete failed");
        }

        @Override
        public ListObjectsV2Response listObjectsV2(ListObjectsV2Request request) {
          return ListObjectsV2Response.builder()
              .contents(S3Object.builder().key("clip-1.mp4").build())
              .isTruncated(false)
              .build();
        }
      };
      CloudflareR2Stager stager = new CloudflareR2Stager(config, operations);

      ComponentException exception = assertThrows(ComponentException.class, () -> stager.purge());

      assertTrue(exception.getMessage().contains("Failed to delete objects from R2"));
    }
  }
}
