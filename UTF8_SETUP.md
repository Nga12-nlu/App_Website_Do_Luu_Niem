# Cấu hình UTF-8 cho Souvenir Shop

Để đảm bảo tiếng Việt hiển thị đúng trên toàn bộ ứng dụng:

## 1. Đã cấu hình trong project
- **CharacterEncodingFilter**: Tự động set UTF-8 cho request/response
- **web.xml**: jsp-config với page-encoding UTF-8
- **pom.xml**: warSourceEncoding và maven-resources UTF-8

## 2. Cấu hình Tomcat (nếu cần)
Nếu URL có tham số tiếng Việt (GET) vẫn bị lỗi, thêm vào file `conf/server.xml` của Tomcat:

```xml
<Connector port="8080" protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443"
           URIEncoding="UTF-8" />
```

Thêm thuộc tính `URIEncoding="UTF-8"` vào thẻ Connector.

## 3. Lưu file với UTF-8
Đảm bảo IDE (IntelliJ/Eclipse) lưu file với encoding UTF-8:
- IntelliJ: File > Settings > Editor > File Encodings > UTF-8
- Eclipse: Window > Preferences > General > Workspace > Text file encoding: UTF-8

