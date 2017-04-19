FROM java:8-jre-alpine

#add timezone and default it to Shanghai
RUN apk --update add --no-cache tzdata
ENV TZ=Asia/Shanghai

RUN mkdir -p /app/log
COPY  juice-rest.jar  /app/juice-rest.jar
COPY entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh

VOLUME ["/app/log"]
WORKDIR /app/

ENTRYPOINT ["./entrypoint.sh"]
CMD []