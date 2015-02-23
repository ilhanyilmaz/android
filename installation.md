install java:
https://www.digitalocean.com/community/tutorials/how-to-install-java-on-ubuntu-with-apt-get
sudo apt-get install libc6:i386 libncurses5:i386 libstdc++6:i386 lib32z1


- download & install android studio
- /etc/environment/  : add ~/android-studio/bin to PATH
- source /etc/environment to reload

- download opencv4android sdk

http://stackoverflow.com/questions/17767557/how-to-use-opencv-in-android-studio-using-gradle-build-tool

1- copy libraries folder into project
2- add into settings.gradle
	include ':libraries:opencv'
3- sync
4- right click project -> open module settings -> dependencies -> + module -> opencv
5- copy jnilibs into app/src/main/


