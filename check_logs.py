import json
with open('/tmp/inv_log_full.json') as f:
    d = json.load(f)
records = d.get('data', {}).get('records', [])
for r in records:
    oid = r.get('operatorId', 'null')
    iid = r.get('inventoryId', '')
    op = r.get('operationType', '')
    qty = r.get('quantity', '')
    remark = r.get('remark', '')
    print(f'inv_id={iid} op_type={op} qty={qty} operator={oid} remark={remark}')
print(f'Total: {d.get("data",{}).get("total",0)}')
