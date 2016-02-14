FROM debian:jessie

ARG packagename

RUN apt-get update && apt-get install -y $packagename && echo "\ndaemon off;" >> /etc/nginx/nginx.conf

CMD ["nginx"]

EXPOSE 80
