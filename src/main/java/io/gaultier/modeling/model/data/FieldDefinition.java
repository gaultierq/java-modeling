package io.gaultier.modeling.model.data;

import java.util.Comparator;

public class FieldDefinition<T, D extends ModelData<D>> implements Comparator<T> {

    private DataDefinition<D> definition;
    private int index;
    private String name;
    private FieldType type;

    private String columnName;
    private boolean persisted;
    private PrimaryKeyType primaryKey = PrimaryKeyType.NO;
    private boolean amf;
    private boolean json;
    private boolean html;
	private Syntax[] syntaxes;

    void init(DataDefinition<D> def, int i, String n, DataType t, Class<? extends Enum<?>> e, boolean list, DataDefinition<? extends ModelData<?>> objCl, boolean per, String col, boolean a, boolean j, boolean h, PrimitiveSubstitution subst, int syntaxesEncoded) {
        definition = def;
        index = i;
        name = n;
        html = h;
        type = new FieldType(t, e, list, objCl, subst);
        persisted = per;
        columnName = col;
        amf = a;
        json = j;
        syntaxes = Syntax.decode(syntaxesEncoded);
    }

    public FieldDefinition<T, D> setPrimaryKey(PrimaryKeyType pk) {
        primaryKey = pk;
        if (pk != PrimaryKeyType.NO) {
            definition.addPrimaryKey(this);
        }
        return this;
    }

    public DataDefinition<D> getDefinition() {
        return definition;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public FieldType getType() {
        return type;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public String getColumnName() {
        return columnName;
    }

    public PrimaryKeyType getPrimaryKey() {
        return primaryKey;
    }

    public boolean isAmf() {
        return amf;
    }
    
    public boolean isDataType() {
        return type.isDataType();
    }

    @SuppressWarnings("unchecked")
    public <DT extends ModelData<DT>> DataList<DT> createList() {
        assert type.isList() : name + " is not a list";
        return (DataList<DT>) type.getObjectType().createList();
    }

    public boolean equals(T va, T vb) {
        return getType().equals(va, vb);
    }

    @Override
    public int compare(T va, T vb) {
        return getType().compareForUser(va, vb);
    }

    @Override
    public String toString() {
        return super.toString() + "(" + getDefinition() + "." + getName() + ")";
    }

    public String getSqlForValue(T value) {
        return getType().getSqlForValue(value);
    }

    public boolean isJson() {
        return json;
    }

    public boolean isHtml() {
        return html;
    }
}
