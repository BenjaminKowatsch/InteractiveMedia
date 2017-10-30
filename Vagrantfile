# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version.
VAGRANTFILE_API_VERSION = "2"

Vagrant.require_version ">= 1.9"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  
  # VM Configuration
  # 
  # use "contrib" version because this includes filesystem type vboxsf
  config.vm.box = "debian/contrib-jessie64"
  config.vm.hostname = "ubuntu-vm"


  # Provider Configuration
  # 
  config.vm.provider "virtualbox" do |v|
    v.memory = 2048
    v.cpus = 2
    v.name = "InteraktiveMedien"
  end


  # Network Configuration
  # 
  # nginx webserver
  config.vm.network "forwarded_port", guest:8080, host: 8080, auto_correct: false

  # backend
  config.vm.network "forwarded_port", guest:8081, host: 8081, auto_correct: false

  # mongodb
  config.vm.network "forwarded_port", guest:27017, host: 27017, auto_correct: false


  # Sync Folder Configuration
  #   
  config.vm.synced_folder ".", "/vagrant", disabled: false
  

  # Provisioning
  # 
  config.vm.provision "shell", path: "provision.sh"

end
