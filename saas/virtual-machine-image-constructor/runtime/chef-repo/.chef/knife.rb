log_level                :info
log_location             STDOUT
node_name                'vmic'
client_key               '/mnt/cephfs/ascetic/vmic/runtime/chef-repo/.chef/vmic.pem'
validation_client_name   'chef-validator'
validation_key           '/mnt/cephfs/ascetic/vmic/runtime/chef-repo/.chef/chef-validator.pem'
chef_server_url          'https://saas-vm-dev:443'
syntax_check_cache_path  '/mnt/cephfs/ascetic/vmic/runtime/chef-repo/.chef/syntax_check_cache'
cookbook_path [ '/mnt/cephfs/ascetic/vmic/runtime/chef-repo/cookbooks' ]
