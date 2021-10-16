EDA for Feature Store
=====================

Build & Run
-----------

아래의 명령어를 이용하여 빌드합니다.

    docker build -t clovafeast-eda-server:latest .

빌드가 완료 되면 아래와 같이 서버를 실행할 수 있습니다.

    docker run -p 8080:8080 clovafeast-eda-server:latest

서버가 실행 되면 `localhost:8080` (또는 Docker daemon이 실행되는 호스트) 주소로 접근 가능합니다.

    curl localhost:8080/api/v1/feature-views
