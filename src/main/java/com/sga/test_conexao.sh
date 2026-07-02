# test_conexao.sh
#!/bin/bash

echo "========================================"
echo "🔍 TESTE DE CONEXÃO MS-SQL"
echo "========================================"

HOST="10.109.1.4"
PORT="1433"

echo ""
echo "📡 1. Testando ping..."
ping -c 3 $HOST
if [ $? -eq 0 ]; then
    echo "   ✅ Host $HOST está acessível"
else
    echo "   ❌ Host $HOST NÃO está acessível"
    exit 1
fi

echo ""
echo "📡 2. Testando porta $PORT..."
timeout 5 telnet $HOST $PORT 2>/dev/null
if [ $? -eq 0 ]; then
    echo "   ✅ Porta $PORT está ABERTA"
else
    echo "   ❌ Porta $PORT está FECHADA ou bloqueada"
fi

echo ""
echo "📡 3. Testando com nc (netcat)..."
nc -zv $HOST $PORT 2>&1

echo ""
echo "========================================"
echo "✅ TESTE CONCLUÍDO"
echo "========================================"