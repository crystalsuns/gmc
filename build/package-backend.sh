#!/bin/bash

#------------------------------------------------------------------
# Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
# This file is part of GlusterFS.
# 
# Gluster Management Gateway is free software; you can redistribute 
# it and/or modify it under the terms of the GNU General Public 
# License as published by the Free Software Foundation; either 
# version 3 of the License, or (at your option) any later version.
# 
# GlusterFS is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see
# <http://www.gnu.org/licenses/>.
#------------------------------------------------------------------

ROOT_DIR=glustermg-backend-${VERSION}
DIR_NAME=${ROOT_DIR}/gmg-scripts
TAR_NAME=${ROOT_DIR}.tar

prepare-script-dir()
{
	if [ -d ${DIR_NAME} ]; then
		rm -rf ${DIR_NAME}
	fi
	mkdir -p ${DIR_NAME}
}

get-scripts()
{
	cd ${DIR_NAME}
	cp ../../src/com.gluster.storage.management.gateway.scripts/src/common/* .
	cp ../../src/com.gluster.storage.management.gateway.scripts/src/backend/* .
	chmod +x *
	cd -
}

#---------------------------------------------
# Main Action Body
#---------------------------------------------
if [ $? -eq 1 ]; then
	DIST_DIR=${1}
fi
echo "Packaging Gluster Management Gateway Back-end Scripts..."

prepare-script-dir
get-scripts

/bin/rm -rf ${TAR_NAME} ${TAR_NAME}.gz
tar cvf ${TAR_NAME} ${ROOT_DIR}
gzip ${TAR_NAME}
if [ ! -z "${DIST_DIR}" ]; then
	mv ${TAR_NAME}.gz ${DIST_DIR}
fi
/bin/rm -rf ${ROOT_DIR}

echo "Done!"
