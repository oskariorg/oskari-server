# Getting started

**Disclaimer:**
*The Vagrant box contains files for oracle jdk 1.7.*
*By using this setup you accept the [Oracle Binary Code License Agreement for Java SE](http://www.oracle.com/technetwork/java/javase/terms/license/index.html).*
*If the inclusion of oracle jdk 1.7 is a problem, please contact paikkatietoikkuna[at]maanmittauslaitos.fi*

We have built a functional Oskari environment in a Vagrant box.

* You'll need to install [Virtualbox](https://www.virtualbox.org/wiki/Downloads) and [Vagrant](http://www.vagrantup.com/downloads.html) to use the box.
* Download the [Vagrantfile](http://oskari.org/boxes/Vagrantfile)
* Open a terminal or command prompt and change into the same directory where the Vagrantfile is.
* Execute `vagrant up` to start the box.
The first time the box is downloaded, so it might take a few minutes depending on your network connection. Once the box is downloaded, it will automatically be registered with vagrant for later use and the startup process begins automatically.
* Execute `vagrant ssh` to access the box and also start the server inside the box.
* Open [this link](http://localhost:8080/oskari-map/) in your favorite browser to view the sample application.

The /vagrant directory is shared between the host and the guest systems, which makes transfering files simple. You can also use ssh for file transfer.

**Note!**
*This is a beta release for the Oskari Ubuntu Precise 10.12 LTS Vagrant Box. It is still a bit rough around the edges and improvements are done gradually. This image is not intended for production use!*