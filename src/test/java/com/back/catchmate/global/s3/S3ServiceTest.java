package com.back.catchmate.global.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @InjectMocks
    private S3Service s3Service;

    @Mock
    private AmazonS3 amazonS3;

    private final String BUCKET_NAME = "test-bucket";
    private final String REGION = "ap-northeast-2";

    @BeforeEach
    void setUp() {
        // @Value 필드에 값 주입
        ReflectionTestUtils.setField(s3Service, "bucket", BUCKET_NAME);
        ReflectionTestUtils.setField(s3Service, "region", REGION);
    }

    @Test
    @DisplayName("파일 업로드 성공 - S3 putObject가 호출되고 올바른 URL이 반환되어야 한다")
    void uploadFile_Success() throws IOException {
        // given
        String originalFileName = "image.jpg";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                originalFileName,
                "image/jpeg",
                "test data".getBytes()
        );

        // putObject는 반환값이 있지만(PutObjectResult), 로직에서 사용하지 않으므로 Mock 동작만 정의 (또는 생략 가능)
        given(amazonS3.putObject(any(), any(), any(), any())).willReturn(new PutObjectResult());

        // when
        String fileUrl = s3Service.uploadFile(file);

        // then
        // 1. AmazonS3 클라이언트의 putObject가 호출되었는지 검증
        verify(amazonS3).putObject(eq(BUCKET_NAME), any(String.class), any(), any());

        // 2. 반환된 URL 형식 검증
        // 형식: https://{bucket}.s3.{region}.amazonaws.com/{uuid}_{fileName}
        assertThat(fileUrl).startsWith("https://" + BUCKET_NAME + ".s3." + REGION + ".amazonaws.com/");
        assertThat(fileUrl).endsWith("_" + originalFileName);
    }
}
