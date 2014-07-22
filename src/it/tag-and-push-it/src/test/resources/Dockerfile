FROM ubuntu:precise

RUN apt-get install -y nginx && echo "\ndaemon off;" >> /etc/nginx/nginx.conf

ENTRYPOINT ["nginx"]

EXPOSE 80