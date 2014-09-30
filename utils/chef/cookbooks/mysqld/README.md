# mysqld Cookbook

Manage your mysqld servers with this cookbook.
Unlike the official [opscode mysql cookbook](https://github.com/opscode-cookbooks/mysql), it doesn't mess with the default mysql configuration. If you do not specify anything explicitly, the defaults of your distribution will be used.
(At least if I do not mess up - Check the [default attributes](https://github.com/chr4/chef-mysqld/blob/master/attributes/defaults.rb), if unsure (and file a pull request if you need to correct anything))

Features

* Defaults to OS settings unless explicitly specified otherwise
* Supports **all** my.cnf settings

Currently tested on Ubuntu, should work on RHEL and Debian as well. [Contributions](https://github.com/chr4/chef-mysqld#contributing) to support other systems are very welcome!

## Requirements

You need to add the following line to your metadata.rb

    depends 'mysqld'


## Attributes

### Configuration

Everything in your my.cnf can be maintained using attributes.
Consider using the provides LWRPs (see below)

If you do not specify anything, the defaults of your os will be used.

This recipe supports **every** setting in the my.cnf.
All your settings will be merged with the systems default, and then written to the my.cnf config file. The packages to install, the path to my.cnf as well as the name of the service are set automatically, and can be overwritten using the following attributes:


```ruby
node['mysqld']['my.cnf_path']
node['mysqld']['service_name']
node['mysqld']['packages']
```

The configuration is stored in the ```node['mysqld']['my.cnf']``` hash, and can be adapted like so


```ruby
# node['mysqld']['my.conf'][<category>][<key>] = <value>
node['mysqld']['my.cnf']['mysqld']['bind-address'] = '0.0.0.0'
```

This will expand to the following in your config file (leaving all other settings untouched)

```
[mysqld]
  bind-address = 0.0.0.0
```

To remove a default option, you can pass `false` or `nil` as the value

```ruby
# Remove deprecated innodb option
default['mysqld']['my.cnf']['mysqld']['innodb_additional_mem_pool_size'] = false
```

As the configuration file is constructed from the config hash, every my.cnf configuration option is supported.


## Recipes

### default

Installs mysql server, and configures it according to the attributes. If no attributes are given, it sticks with the systems default


# Contributing

Contributions are very welcome!

1. Fork the repository on Github
2. Create a named feature branch (like `add_component_x`)
3. Write you change
4. Write tests for your change (if applicable)
5. Run the tests, ensuring they all pass
6. Submit a Pull Request using Github


# License and Authors

Authors: Chris Aumann <me@chr4.org>

License: GPLv3
