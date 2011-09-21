#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Storage Platform.
#

import os
import sys
p1 = os.path.abspath(os.path.dirname(sys.argv[0]))
p2 = "%s/common" % os.path.dirname(p1)
if not p1 in sys.path:
    sys.path.append(p1)
if not p2 in sys.path:
    sys.path.append(p2)
import dbus
import socket
import re
import Utils
import Protocol
import DiskUtils
from NetworkUtils import *
from Disk import *
from XmlHandler import ResponseXml
from optparse import OptionParser


def getDiskDom():
    diskInfo = DiskUtils.getDiskInfo()
    if not diskInfo:
        return None
    procMdstat = DiskUtils.getProcMdstat()
    diskDom = Protocol.XDOM()
    disksTag = diskDom.createTag("disks", None)
    diskTagDict = {}
    raidDisksTag = diskDom.createTag("raidDisks", None)
    for raidDiskName, raidDisk in procMdstat.iteritems():
        raidDiskTag = diskDom.createTag("disk", None)
        raidDiskTag.appendChild(diskDom.createTag("name", raidDiskName))
        raidDiskTag.appendChild(diskDom.createTag("description", diskInfo[raidDiskName]['Description']))
        raidDiskTag.appendChild(diskDom.createTag("uuid", diskInfo[raidDiskName]['Uuid']))
        raidDiskTag.appendChild(diskDom.createTag("type", "UNKNOWN"))
        raidDiskTag.appendChild(diskDom.createTag("mountPoint", diskInfo[raidDiskName]['MountPoint']))
        raidDiskTag.appendChild(diskDom.createTag("fsType", diskInfo[raidDiskName]['FsType']))
        if diskInfo[raidDiskName]['FsType']:
            raidDiskTag.appendChild(diskDom.createTag("status", "INITIALIZED"))
        else:
            raidDiskTag.appendChild(diskDom.createTag("status", "UNINITIALIZED"))
        raidDiskTag.appendChild(diskDom.createTag("interface"))
        raidDiskTag.appendChild(diskDom.createTag("fsVersion"))
        raidDiskTag.appendChild(diskDom.createTag("size", diskInfo[raidDiskName]['Size'] / 1024.0))
        raidDiskTag.appendChild(diskDom.createTag("spaceInUse", diskInfo[raidDiskName]['SpaceInUse']))
        raidDisksTag.appendChild(raidDiskTag)
        for raidMember in raidDisk['Member']:
            # Case1: Raid array member is a disk. The following code will add the disk details under a disk tag
            if diskInfo.has_key(raidMember):
                diskTag = diskDom.createTag("disk", None)
                diskTag.appendChild(diskDom.createTag("name", raidMember))
                diskTag.appendChild(diskDom.createTag("description", diskInfo[raidMember]["Description"]))
                diskTag.appendChild(diskDom.createTag("uuid", diskInfo[raidMember]["Uuid"]))
                if DiskUtils.isDiskInFormatting(raidMember):
                    diskTag.appendChild(diskDom.createTag("status", "INITIALIZING"))
                else:
                    if diskInfo[raidMember]["FsType"]:
                        diskTag.appendChild(diskDom.createTag("status", "INITIALIZED"))
                    else:
                        diskTag.appendChild(diskDom.createTag("status", "UNINITIALIZED"))
                diskTag.appendChild(diskDom.createTag("interface", diskInfo[raidMember]["Interface"]))
                diskTag.appendChild(diskDom.createTag("mountPoint", diskInfo[raidMember]["MountPoint"]))
                if diskInfo[raidMember]["FsType"]:
                    diskTag.appendChild(diskDom.createTag("type", "DATA"))
                else:
                    diskTag.appendChild(diskDom.createTag("type", "UNKNOWN"))
                diskTag.appendChild(diskDom.createTag("fsType", diskInfo[raidMember]["FsType"]))
                diskTag.appendChild(diskDom.createTag("fsVersion", diskInfo[raidMember]["FsVersion"]))
                diskTag.appendChild(diskDom.createTag("size", diskInfo[raidMember]["Size"] / 1024.0))
                diskTag.appendChild(diskDom.createTag("spaceInUse", diskInfo[raidMember]["SpaceInUse"]))
                raidDiskTag.appendChild(diskTag)
                del diskInfo[raidMember]
                continue
            # Case2: Raid array member is a partition. The following code will add the partition and its corresponding disk its belong to.
            for disk, item in diskInfo.iteritems():
                if not item['Partitions'].has_key(raidMember):
                    continue
                if not diskTagDict.has_key(disk):
                    diskTag = diskDom.createTag("disk", None)
                    diskTag.appendChild(diskDom.createTag("name", disk))
                    diskTag.appendChild(diskDom.createTag("description", item["Description"]))
                    diskTag.appendChild(diskDom.createTag("uuid", item["Uuid"]))
                    diskTag.appendChild(diskDom.createTag("status", "INITIALIZED"))
                    diskTag.appendChild(diskDom.createTag("interface", item["Interface"]))
                    diskTag.appendChild(diskDom.createTag("mountPoint"))
                    diskTag.appendChild(diskDom.createTag("type", "DATA"))
                    diskTag.appendChild(diskDom.createTag("fsType", item["FsType"]))
                    diskTag.appendChild(diskDom.createTag("fsVersion", item["FsVersion"]))
                    diskTag.appendChild(diskDom.createTag("size", item["Size"] / 1024.0))
                    diskTag.appendChild(diskDom.createTag("spaceInUse", item["SpaceInUse"]))
                    partitionsTag = diskDom.createTag("partitions", None)
                    diskTag.appendChild(partitionsTag)
                    raidDiskTag.appendChild(diskTag)
                    # Constructed disk tag will be added to the dictonary.
                    # This will be used to keep add all the corresponding partitions tags of the disk to the disk tag.
                    diskTagDict[disk] = {'diskTag': diskTag, 'partitionsTag': partitionsTag}
                # adding partition details under this disk tag
                partitionTag = diskDom.createTag("partition", None)
                partitionTag.appendChild(diskDom.createTag("name", raidMember))
                partitionTag.appendChild(diskDom.createTag("uuid", item['Partitions'][raidMember]["Uuid"]))
                partitionTag.appendChild(diskDom.createTag("fsType", item['Partitions'][raidMember]["FsType"]))
                if item['Partitions'][raidMember]["FsType"]:
                    partitionTag.appendChild(diskDom.createTag("status", "INITIALIZED"))
                    partitionTag.appendChild(diskDom.createTag("type", "DATA"))
                else:
                    partitionTag.appendChild(diskDom.createTag("status", "UNINITIALIZED"))
                    partitionTag.appendChild(diskDom.createTag("type", "UNKNOWN"))
                partitionTag.appendChild(diskDom.createTag("mountPoint", item['Partitions'][raidMember]['MountPoint']))
                partitionTag.appendChild(diskDom.createTag("size", item['Partitions'][raidMember]["Size"] / 1024.0))
                partitionTag.appendChild(diskDom.createTag("spaceInUse", item['Partitions'][raidMember]["SpaceInUse"]))
                diskTagDict[disk]['partitionsTag'].appendChild(partitionTag)
                # deleting partition entry of a raid member from diskInfo (item['Partitions'])
                del item['Partitions'][raidMember]
        del diskInfo[raidDiskName]
    disksTag.appendChild(raidDisksTag)
    for diskName, value in diskInfo.iteritems():
        diskTag = diskDom.createTag("disk", None)
        diskTag.appendChild(diskDom.createTag("name", diskName))
        diskTag.appendChild(diskDom.createTag("description", value["Description"]))
        diskTag.appendChild(diskDom.createTag("uuid", value["Uuid"]))
        if DiskUtils.isDiskInFormatting(diskName):
            status = "INITIALIZING"
        else:
            if value["FsType"]:
                status = "INITIALIZED"
            else:
                status = "UNINITIALIZED"
        diskTag.appendChild(diskDom.createTag("status", status))
        diskTag.appendChild(diskDom.createTag("interface", value["Interface"]))
        if value["MountPoint"] and value["MountPoint"] in ["/", "/boot"]:
            diskTag.appendChild(diskDom.createTag("type", "BOOT"))
        elif "UNINITIALIZED" == status:
            diskTag.appendChild(diskDom.createTag("type", "UNKNOWN"))
        else:
            diskTag.appendChild(diskDom.createTag("type", "DATA"))
        diskTag.appendChild(diskDom.createTag("fsType", value["FsType"]))
        diskTag.appendChild(diskDom.createTag("fsVersion", value["FsVersion"]))
        diskTag.appendChild(diskDom.createTag("mountPoint", value["MountPoint"]))
        diskTag.appendChild(diskDom.createTag("size", value["Size"] / 1024.0))
        diskTag.appendChild(diskDom.createTag("spaceInUse", value["SpaceInUse"]))
        partitionsTag = diskDom.createTag("partitions", None)
        diskTag.appendChild(partitionsTag)
        for partName, partValues in value['Partitions'].iteritems():
            partitionTag = diskDom.createTag("partition", None)
            partitionTag.appendChild(diskDom.createTag("name", partName))
            partitionTag.appendChild(diskDom.createTag("uuid", partValues["Uuid"]))
            partitionTag.appendChild(diskDom.createTag("fsType", partValues["FsType"]))
            if partValues["FsType"]:
                partitionTag.appendChild(diskDom.createTag("status", "INITIALIZED"))
                partitionTag.appendChild(diskDom.createTag("type", "DATA"))
            else:
                partitionTag.appendChild(diskDom.createTag("status", "UNINITIALIZED"))
                partitionTag.appendChild(diskDom.createTag("type", "UNKNOWN"))
            partitionTag.appendChild(diskDom.createTag("mountPoint", partValues['MountPoint']))
            partitionTag.appendChild(diskDom.createTag("size", partValues["Size"] / 1024.0))
            partitionTag.appendChild(diskDom.createTag("spaceInUse", partValues["SpaceInUse"]))
            partitionsTag.appendChild(partitionTag)
            continue
        disksTag.appendChild(diskTag)
    diskDom.addTag(disksTag)
    return diskDom


def getServerDetails(listall):
    serverName = socket.getfqdn()
    meminfo = Utils.getMeminfo()
    cpu = Utils.getCpuUsageAvg()
    nameServerList, domain, searchDomain = readResolvConfFile()
    if not domain:
        domain = [None]

    responseDom = ResponseXml()
    serverTag = responseDom.appendTagRoute("server")
    serverTag.appendChild(responseDom.createTag("name", serverName))
    serverTag.appendChild(responseDom.createTag("domainname", domain[0]))
    if Utils.runCommand("pidof glusterd") == 0:
        serverTag.appendChild(responseDom.createTag("status", "ONLINE"))
    else:
        serverTag.appendChild(responseDom.createTag("status", "OFFLINE"))
    serverTag.appendChild(responseDom.createTag("glusterFsVersion", Utils.getGlusterVersion()))
    serverTag.appendChild(responseDom.createTag("cpuUsage", str(cpu)))
    serverTag.appendChild(responseDom.createTag("totalMemory", str(convertKbToMb(meminfo['MemTotal']))))
    serverTag.appendChild(responseDom.createTag("memoryInUse", str(convertKbToMb(meminfo['MemUsed']))))
    serverTag.appendChild(responseDom.createTag("uuid", None))

    for dns in nameServerList:
        serverTag.appendChild(responseDom.createTag("dns%s" % str(nameServerList.index(dns) +1) , dns))

    #TODO: probe and retrieve timezone, ntp-server details and update the tags

    deviceList = {}
    interfaces = responseDom.createTag("networkInterfaces", None)
    for device in getNetDeviceList():
        if device["model"] in ['LOCAL', 'IPV6-IN-IPV4']:
            continue
        deviceList[device["device"]] = device
        try:
            macAddress = open("/sys/class/net/%s/address" % device["device"]).read().strip()
        except IOError, e:
            continue
        interfaceTag = responseDom.createTag("networkInterface", None)
        interfaceTag.appendChild(responseDom.createTag("name",  device["device"]))
        interfaceTag.appendChild(responseDom.createTag("hwAddr",macAddress))
        interfaceTag.appendChild(responseDom.createTag("speed", device["speed"]))
        interfaceTag.appendChild(responseDom.createTag("model", device["model"]))
        if deviceList[device["device"]]:
            if deviceList[device["device"]]["onboot"]:
                interfaceTag.appendChild(responseDom.createTag("onboot", "yes"))
            else:
                interfaceTag.appendChild(responseDom.createTag("onBoot", "no"))
            interfaceTag.appendChild(responseDom.createTag("bootProto", deviceList[device["device"]]["bootproto"]))
            interfaceTag.appendChild(responseDom.createTag("ipAddress",    deviceList[device["device"]]["ipaddr"]))
            interfaceTag.appendChild(responseDom.createTag("netMask",   deviceList[device["device"]]["netmask"]))
            interfaceTag.appendChild(responseDom.createTag("defaultGateway",   deviceList[device["device"]]["gateway"]))
            if deviceList[device["device"]]["mode"]:
                interfaceTag.appendChild(responseDom.createTag("mode",   deviceList[device["device"]]["mode"]))
            if deviceList[device["device"]]["master"]:
                interfaceTag.appendChild(responseDom.createTag("bonding", "yes"))
                spliter = re.compile(r'[\D]')
                interfaceTag.appendChild(responseDom.createTag("bondid", spliter.split(device["master"])[-1]))            
        else:
            interfaceTag.appendChild(responseDom.createTag("onBoot",    "no"))
            interfaceTag.appendChild(responseDom.createTag("bootProto", "none"))
        interfaces.appendChild(interfaceTag)
    serverTag.appendChild(interfaces)

    responseDom.appendTag(serverTag)
    serverTag.appendChild(responseDom.createTag("numOfCPUs", int(os.sysconf('SC_NPROCESSORS_ONLN'))))

    diskDom = getDiskDom()
    if not diskDom:
        sys.stderr.write("No disk found!")
        Utils.log("Failed to get disk details")
        sys.exit(2)

    serverTag.appendChild(diskDom.getElementsByTagRoute("disks")[0])
    return serverTag

def main():
    parser = OptionParser()
    parser.add_option("-N", "--only-data-disks",
                      action="store_false", dest="listall", default=True,
                      help="List only data disks")

    (options, args) = parser.parse_args()
    responseXml = getServerDetails(options.listall)
    if responseXml:
        print responseXml.toxml()

    sys.exit(0)

if __name__ == "__main__":
    main()
