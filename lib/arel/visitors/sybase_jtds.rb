module Arel
  module Visitors
    class SybaseJtds < Arel::Visitors::ToSql
      def visit_Arel_Nodes_SelectStatement o
        limit  = o.limit
        offset = o.offset
        o.limit = o.offset = nil
        sql = super

        if limit && !offset
          limit_and_no_offset sql, limit
        end

        sql
      end

      def limit_and_no_offset sql, limit
        if sql =~ /DISTINCT /
          sql.gsub!(/DISTINCT /, "DISTINCT TOP #{limit} ")
        else
          sql.gsub!(/SELECT /, "SELECT TOP #{limit} ")
        end
      end
    end
  end
end
