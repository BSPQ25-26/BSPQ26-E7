FROM nginx:1.27-alpine

RUN printf '%s\n' \
	'<!doctype html>' \
	'<html lang="en">' \
	'<head><meta charset="utf-8"><title>LabLend Frontend</title></head>' \
	'<body><h1>LabLend Frontend</h1><p>Frontend container is running.</p></body>' \
	'</html>' \
	> /usr/share/nginx/html/index.html

EXPOSE 80