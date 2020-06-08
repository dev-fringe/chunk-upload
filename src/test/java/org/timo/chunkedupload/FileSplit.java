package org.timo.chunkedupload;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;
import org.timo.chunkedupload.model.RangeData;
import org.timo.chunkedupload.service.FileService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ComponentScan("org.timo.chunkedupload.service")
public class FileSplit implements InitializingBean {
	
	@Bean public AsyncRestTemplate asyncRestTemplate() {return new AsyncRestTemplate();}
	
	private final AsyncRestTemplate asyncRestTemplate;
	
	private final FileService fileService;

    public static void main(String[] args) throws IOException {
    	new AnnotationConfigApplicationContext(FileSplit.class).getBean(FileSplit.class);   
    }

    public  HttpEntity<Resource> buildFilePart(Resource resource, MediaType mediaType) {
        HttpHeaders headers = null;
        if (mediaType != null) {
            headers = new HttpHeaders();
            headers.setContentType(mediaType);
        }
        return new HttpEntity<>(resource, headers);
    }
    
	public void afterPropertiesSet() throws Exception {
//    	File file = new File(System.getProperty("user.home") +"\\Downloads\\jdk-14.0.1_windows-x64_bin.exe");
    	File file = new File(System.getProperty("user.home") +"\\Downloads\\CP - AO Demo image 3.vmdk");
    	List<RangeData> datas = fileService.splitFile(file);
        for (RangeData rangeData : datas) {
    		Resource resource = new FileSystemResource(rangeData.getSplitedFilename());
    		HttpHeaders multipartHeaders = new HttpHeaders();
    		multipartHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
    		multipartHeaders.setContentDispositionFormData("attachment", rangeData.getFilename());
    		multipartHeaders.add("Content-Range",String.format("bytes %s-%s/%s", rangeData.getBegin(), rangeData.getEnd(), rangeData.getTotalSize()));  
    		LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
    		parts.add("files", buildFilePart(resource, MediaType.TEXT_PLAIN));
			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, multipartHeaders);
			ListenableFuture<ResponseEntity<Map>> future = asyncRestTemplate.postForEntity("http://localhost:8080/screenfood3/chunked-upload",requestEntity, Map.class);
    		ResponseEntity<Map> entity = future.get();  
    		System.out.println(entity.getBody());
		}
	}
}