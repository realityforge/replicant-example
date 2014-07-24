workspace_dir = File.expand_path(File.dirname(__FILE__) + '/..')
plugins_dir = "#{workspace_dir}/vendor/plugins"
generated_database_dir = "#{workspace_dir}/database/generated"
database_search_dirs = [generated_database_dir, "#{workspace_dir}/database"]

$LOAD_PATH.unshift("#{plugins_dir}/dbt/lib")
$LOAD_PATH.unshift("#{plugins_dir}/domgen/lib")

require 'dbt'
require 'domgen'

Domgen::Sql.dialect = Domgen::Sql::PgDialect
Domgen::LoadSchema.new("#{workspace_dir}/architecture.rb")
Domgen::GenerateTask.new(:Tyrell, :sql, [:pgsql], generated_database_dir)

Dbt::Config.environment = ENV['DB_ENV'] if ENV['DB_ENV']
Dbt::Config.driver = 'postgres'
Dbt::Config.config_filename = File.expand_path("#{workspace_dir}/config/database.yml")

Dbt.add_database(:default) do |database|
  database.search_dirs = database_search_dirs
  database.enable_domgen(:Tyrell, 'domgen:load', 'domgen:sql')
  database.version = '1'
end
