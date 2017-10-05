require 'dbt'

Dbt.add_database(:default) do |database|
  database.version = '1'
end
