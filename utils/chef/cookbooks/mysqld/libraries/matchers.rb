# Matchers for chefspec
if defined?(ChefSpec)
  def create_mysqld
    ChefSpec::Matchers::ResourceMatcher.new(:mysqld, :create, 'default')
  end
end
