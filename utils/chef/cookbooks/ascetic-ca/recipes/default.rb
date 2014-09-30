#
# Cookbook Name:: ascetic-ca
# Recipe:: default
#
# Copyright 2014, Michael Kammer
#

cookbook_file "/usr/local/share/ca-certificates/Ascetic_TUB_CA.crt" do
  source "Ascetic.crt"
  mode 0644
  owner "root"
  group "root"
end

script "install_ca_cert" do
  interpreter "bash"
  user "root"
  group "root"
  cwd "/"
  not_if "test -f /etc/ssl/certs/Ascetic_TUB_CA.pem"
  code <<-EOH
/usr/sbin/update-ca-certificates
  EOH
end

