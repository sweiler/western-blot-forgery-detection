western-blot-forgery-detection
==============================

This is a forgery-detection web app that is built specifically for western-blot images.

The folder web-app contains all source code and libraries for running the web application. Additionally you need one or more worker clients, which do the analysis. The source code for these workers can be found in the "worker" directory.

Both folders are eclipse and maven projects.

If you want to run this project, you should make use of the "install" folder, which contains a long INSTALL readme and also all files needed.
To run the application, you must have some SSL certificates. Self-signed certificates together with the corresponding private keys are contained in the "install" folder, but you should replace these certificates with others for production.
Not only to have real signed certificates, but to not rely on public available keys. And since this project is published, those keys are publicly available too.

