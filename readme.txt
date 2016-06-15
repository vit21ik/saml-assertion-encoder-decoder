Creating private key:

openssl genrsa -out privatekey.pem 2048

Creating public certificate:

openssl req -new -x509 -key privatekey.pem -out publickey.cer -days 999