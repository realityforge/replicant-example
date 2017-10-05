require 'dbt'
require 'buildr_plus'

Dbt.add_database(:default) do |database|
  database.version = '1'
end
