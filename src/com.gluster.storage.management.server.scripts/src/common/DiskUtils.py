#  Copyright (c) 2010 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Storage Platform.
#
#  Gluster Storage Platform is free software; you can redistribute it
#  and/or modify it under the terms of the GNU General Public License
#  as published by the Free Software Foundation; either version 3 of
#  the License, or (at your option) any later version.
#
#  Gluster Storage Platform is distributed in the hope that it will be
#  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
#  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see
#  <http://www.gnu.org/licenses/>.

import os
import glob
import dbus

import Globals
from Utils import *

ONE_MB_SIZE = 1048576


def _stripDev(device):
    if isString(device) and device.startswith("/dev/"):
        return device[5:]
    return device


def _addDev(deviceName):
    if isString(deviceName) and not deviceName.startswith("/dev/"):
        return "/dev/" + deviceName
    return deviceName


def getDeviceName(device):
    if type(device) == type([]):
        nameList = []
        for d in device:
            nameList.append(_stripDev(d))
        return nameList
    return _stripDev(device)


def getDevice(deviceName):
    if isString(deviceName):
        return _addDev(deviceName)
    if type(deviceName) == type([]):
        nameList = []
        for d in deviceName:
            nameList.append(_addDev(d))
        return nameList
    return _addDev(deviceName)


def getDiskPartitionByUuid(uuid):
    uuidFile = "/dev/disk/by-uuid/%s" % uuid
    if os.path.exists(uuidFile):
        return getDeviceName(os.path.realpath(uuidFile))
    return None


def getUuidByDiskPartition(device):
    for uuidFile in glob.glob("/dev/disk/by-uuid/*"):
        if os.path.realpath(uuidFile) == device:
            return os.path.basename(uuidFile)
    return None


def getDiskPartitionUuid(partition):
    log("WARNING: getDiskPartitionUuid() is deprecated by getUuidByDiskPartition()")
    return getUuidByDiskPartition(partition)


def getDiskPartitionByLabel(label):
    ## TODO: Finding needs to be enhanced
    labelFile = "/dev/disk/by-label/%s" % label
    if os.path.exists(labelFile):
        return getDeviceName(os.path.realpath(labelFile))
    return None


def getDeviceByLabel(label):
    log("WARNING: getDeviceByLabel() is deprecated by getDiskPartitionByLabel()")
    return getDiskPartitionByLabel(label)


def getDiskPartitionLabel(device):
    rv = runCommandFG(["sudo", "e2label", device], stdout=True)
    if rv["Status"] == 0:
        return rv["Stdout"].strip()
    return False


def getRootPartition(fsTabFile=Globals.FSTAB_FILE):
    fsTabEntryList = readFsTab(fsTabFile)
    for fsTabEntry in fsTabEntryList:
        if fsTabEntry["MountPoint"] == "/":
            if fsTabEntry["Device"].startswith("UUID="):
                return getDiskPartitionByUuid(fsTabEntry["Device"].split("UUID=")[-1])
            if fsTabEntry["Device"].startswith("LABEL="):
                return getDiskPartitionByLabel(fsTabEntry["Device"].split("LABEL=")[-1])
            return getDeviceName(fsTabEntry["Device"])
    return None


def getOsDisk():
    log("WARNING: getOsDisk() is deprecated by getRootPartition()")
    return getRootPartition()


def getDiskList(diskDeviceList=None):
    diskDeviceList = getDevice(diskDeviceList)
    if isString(diskDeviceList):
        diskDeviceList = [diskDeviceList]

    dbusSystemBus = dbus.SystemBus()
    halObj = dbusSystemBus.get_object("org.freedesktop.Hal",
                                      "/org/freedesktop/Hal/Manager")
    halManager = dbus.Interface(halObj, "org.freedesktop.Hal.Manager")
    storageUdiList = halManager.FindDeviceByCapability("storage")

    diskList = []
    for udi in storageUdiList:
        halDeviceObj = dbusSystemBus.get_object("org.freedesktop.Hal", udi)
        halDevice = dbus.Interface(halDeviceObj,
                                   "org.freedesktop.Hal.Device")
        if halDevice.GetProperty("storage.drive_type") == "cdrom" or \
                halDevice.GetProperty("block.is_volume"):
            continue

        disk = {}
        disk["Device"] = str(halDevice.GetProperty('block.device'))
        if diskDeviceList and disk["Device"] not in diskDeviceList:
            continue
        disk["Description"] = str(halDevice.GetProperty('storage.vendor')) + " " + str(halDevice.GetProperty('storage.model'))
        if halDevice.GetProperty('storage.removable'):
            disk["Size"] = long(halDevice.GetProperty('storage.removable.media_size'))
        else:
            disk["Size"] = long(halDevice.GetProperty('storage.size'))
        disk["Interface"] = str(halDevice.GetProperty('storage.bus'))
        disk["DriveType"] = str(halDevice.GetProperty('storage.drive_type'))
        partitionList = []
        partitionUdiList = halManager.FindDeviceStringMatch("info.parent", udi)
        for partitionUdi in partitionUdiList:
            partitionHalDeviceObj = dbusSystemBus.get_object("org.freedesktop.Hal",
                                                             partitionUdi)
            partitionHalDevice = dbus.Interface(partitionHalDeviceObj,
                                                "org.freedesktop.Hal.Device")
            if not partitionHalDevice.GetProperty("block.is_volume"):
                continue
            partition = {}
            partition["Device"] = str(partitionHalDevice.GetProperty('block.device'))
            partition["Uuid"] = str(partitionHalDevice.GetProperty('volume.uuid'))
            partition["Size"] = long(partitionHalDevice.GetProperty('volume.size'))
            partition["Fstype"] = str(partitionHalDevice.GetProperty('volume.fstype'))
            partition["Fsversion"] = str(partitionHalDevice.GetProperty('volume.fsversion'))
            partition["Label"] = str(partitionHalDevice.GetProperty('volume.label'))
            partition["Used"] = 0L
            if partitionHalDevice.GetProperty("volume.is_mounted"):
                rv = runCommandFG(["df", str(partitionHalDevice.GetProperty('volume.mount_point'))], stdout=True)
                if rv["Status"] == 0:
                    try:
                        partition["Used"] = long(rv["Stdout"].split("\n")[1].split()[2])
                    except IndexError:
                        pass
                    except ValueError:
                        pass
            partitionList.append(partition)
        disk["Partitions"] = partitionList
        diskList.append(disk)
    return diskList


def getMountPointByUuid(partitionUuid):
    # check uuid in etc/fstab
    try:
        fstabEntries = open(Globals.FSTAB_FILE).readlines()
    except IOError:
        fstabEntries = []
    found = False
    for entry in fstabEntries:
        entry = entry.strip()
        if not entry:
            continue
        if entry.split()[0] == "UUID=" + partitionUuid:
            return entry.split()[1]
    return None
