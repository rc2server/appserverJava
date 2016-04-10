# Setting up ubuntu vm

apt-get install ssh
configure sshd
sudo add-apt-repository "deb http://apt.postgresql.org/pub/repos/apt/ trusty-pgdg main"
wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
sudo add-apt-repository ppa:ubuntu-toolchain-r/test
sudo apt-get update

### install java
	download linux x64 jdk from oracle 
	unzip in /usr/local
	create link to /usr/local/java
	sudo update-alternatives --install "/usr/bin/java" "java" "/usr/local/java/bin/java" 2000
	sudo update-alternatives --install "/usr/bin/javac" "javac" "/usr/local/java/bin/javac" 2000
	sudo update-alternatives --install "/usr/bin/javaws" "javaws" "/usr/local/java/bin/javaws" 2000
	add JAVA_HOME=/usr/local/java to profile

### install packages
sudo apt-get install cmake libboost-all-dev libboost-dev uuid-dev libssl-dev
sudo apt-get install build-essential autoconf automake pkg-config git-core libcurl4-openssl-dev
#probably don't need all the libs, but small so no harm
sudo apt-get install gdb libgtest-dev libcppunit-dev libcunit1-dev libgoogle-glog-dev Xvfb postgresql-9.4 postgresql-client-9.4
sudo apt-get install libtiff4-dev libcairo2-dev fonts-inconsolata xorg-dev texlive texinfo maven gradle
sudo apt-get install gfortran libreadline-dev texlive-fonts-extra

#update gcc
sudo apt-get install g++-4.9
sudo update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-4.8 10
sudo update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-4.9 20
sudo update-alternatives --install /usr/bin/g++ g++ /usr/bin/gcc-4.8 10
sudo update-alternatives --install /usr/bin/g++ g++ /usr/bin/gcc-4.9 20

### build gtest library files
cd /usr/src/gtest
sudo cmake CMakeLists.txt
sudo make
sudo cp *.a /usr/lib

###install libevent2
	download https://github.com/libevent/libevent/releases/download/release-2.0.22-stable/libevent-2.0.22-stable.tar.gz
	tar xvf libevent-2.0.22-stable.tar.gz
	cd libevent-2.0.22-stable
	./configure
	sudo make install
	sudo ldconfig

### install R
	 ./configure --enable-R-shlib --with-blas --with-x=yes --with-lapack --enable-prebuilt-html LDFLAGS=-Wl,-Bsymbolic-functions
	 install packages: knitr
	 install Rcpp (I do from source, likely doesn't matter)
	 install RInside from source. Edit inst/include/RInsideConfig.h and uncomment #define RINSIDE_CALLBACKS
	 sudo ln -s /usr/local/lib/R/share/texmf/tex/latex/  /usr/share/texmf/tex/latex/R
	 sudo texhash
	 
### rc2compute
	install package from R/rc2

download latest source (currently https://cran.r-project.org/src/base/R-3/R-3.2.4.tar.gz)
	./configure --enable-R-shlib --with-blas --with-x=yes --with-lapack --enable-prebuilt-html LDFLAGS=-Wl,-Bsymbolic-functions
	



## to get a list of packages installed run
	zcat /var/log/apt/history.log.*.gz | cat - /var/log/apt/history.log | grep -Po '^Commandline:(?= apt-get)(?=.* install ) \K.*'


## to update

sudo apt-get update        # Fetches the list of available updates
sudo apt-get upgrade       # Strictly upgrades the current packages
sudo apt-get dist-upgrade  # Installs updates (new ones)

## packages installed on rc2dev, lots likely not used

apt-get install ntp
apt-get install maven
apt-get install libcurl4-openssl-dev
apt-get install oracle-java8-set-default
apt-get install postgresql-client-9.4
apt-get install libpqtypes-dev
apt-get install valgrind
apt-get install postgresql-9.4
apt-get install couchdb
apt-get install texinfo
apt-get install texlive
apt-get install xorg-dev
apt-get install libcairo2-dev
apt-get install fonts-inconsolata
apt-get install libtiff4-dev
apt-get install Xvfb
apt-get install libpq-dev
apt-get install libpq-dev
apt-get install libgoogle-glog-dev
apt-get install libcunit1-dev
apt-get install libcppunit-dev
apt-get install libgtest-dev
apt-get install gdb
apt-get -y install virtualbox-guest-dkms virtualbox-guest-utils linux-headers-generic nfs-common python-apport puppet byobu juju ruby libnss-myhostname chef
apt-get -y -q install oracle-java8-installer
apt-get install -y build-essential autoconf automake pkg-config git-core
apt-get install -y r-base
apt-get install libboost-dev
apt-get install uuid-dev
apt-get install libboost-all-dev
apt-get install cmake
