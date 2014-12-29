plugins_dir = "#{File.expand_path(File.dirname(__FILE__) + '/..')}/vendor/plugins"

$LOAD_PATH.unshift("#{plugins_dir}/dbt/lib")
$LOAD_PATH.unshift("#{plugins_dir}/domgen/lib")

require 'dbt'
require 'domgen'

Domgen::Build.define_load_task

Domgen::Build.define_generate_task([:pgsql], :key => :sql, :target_dir => 'database/generated')

Dbt::Config.environment = ENV['DB_ENV'] if ENV['DB_ENV']
Dbt::Config.driver = 'postgres'

Dbt.add_database(:default) do |database|
  database.search_dirs = %w(database/generated database)
  database.enable_domgen
  database.version = '1'
end
