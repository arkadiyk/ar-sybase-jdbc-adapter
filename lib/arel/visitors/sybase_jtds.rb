module Arel
  module Visitors
    class SybaseJtds < Arel::Visitors::ToSql

      def select_count? o
        sel = o.cores.length == 1 && o.cores.first
        projections = sel.projections.length == 1 && sel.projections
        Arel::Nodes::Count === projections.first
      end


      def visit_Arel_Nodes_SelectStatement o
        if o.offset || (o.limit && select_count?(o))
          o.offset.expr += 1 if o.offset
          sql = super  # if offset OR (limit & count) use the Java limit/offset/count parser
        else
          limit  = o.limit.expr
          o.limit = nil
          sql = super

          if limit
            limit_and_no_offset sql, limit
          end
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
