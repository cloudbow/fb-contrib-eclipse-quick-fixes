echo $(pwd)
echo $(ls -lR ..)
if [ ! -d "../eclipse-relang/plugins" ]; then
  rm -rf ../eclipse-relang
fi

if [ ! -d "../eclipse-relang" ]; then
  wget http://git.eclipse.org/c/platform/eclipse.platform.releng.basebuilder.git/snapshot/R38M6PlusRC3G.tar.gz -O - | tar -xz --directory ../
  mv ../R38M6PlusRC3G ../eclipse-relang
  echo $(pwd)
  echo $(ls -lR ..)
fi