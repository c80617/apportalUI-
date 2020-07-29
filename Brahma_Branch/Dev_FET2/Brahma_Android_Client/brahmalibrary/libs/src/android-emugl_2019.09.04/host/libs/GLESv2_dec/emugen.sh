BASEDIR=$(pwd)
EMUGEN=/home/sting/aemu/emu-master-dev/external/qemu/objs/emugen
DST_DIR=${BASEDIR}/intermediates-dir
SRC_DIR=${BASEDIR}
BASENAME=gles2 

set -x

mkdir -p ${DST_DIR}

# emugen -D <dst-dir> -I <src-dir> <basename>
${EMUGEN} -D ${DST_DIR} -i ${SRC_DIR} ${BASENAME}

set +x

