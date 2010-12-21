module ::ArJdbc
  extension :SybaseJtds do |name|
    if name =~ /sybase_jtds/i
      require 'arjdbc/sybase_jtds'
      true
    end
  end
end
