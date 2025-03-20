python
import pandas as pd
import matplotlib.pyplot as plt
import glob
import re

# Função para extrair dados dos arquivos de resultados
def extract_data(file_pattern):
    data = []
    for filename in glob.glob(file_pattern):
        with open(filename, 'r') as file:
            for line in file:
                match = re.search(r'Running version (\d+) with matrix size (\d+) x \d+, run \d+: ([\d.]+), L1 DCM: (\d+), L2 DCM: (\d+)', line)
                if match:
                    version = int(match.group(1))
                    size = int(match.group(2))
                    exec_time = float(match.group(3))
                    l1_dcm = int(match.group(4))
                    l2_dcm = int(match.group(5))
                    data.append((version, size, exec_time, l1_dcm, l2_dcm))
    return data

# Extrair dados dos arquivos de resultados
data = extract_data('results-cpp-*.txt')

# Criar um DataFrame a partir dos dados extraídos
df = pd.DataFrame(data, columns=['Version', 'Size', 'ExecutionTime', 'L1DCM', 'L2DCM'])

# Calcular a média para cada combinação de versão e tamanho
averages = df.groupby(['Version', 'Size']).mean().reset_index()

# Criar gráficos
plt.figure(figsize=(15, 5))

# Gráfico do tempo de execução
plt.subplot(1, 3, 1)
for version in averages['Version'].unique():
    subset = averages[averages['Version'] == version]
    plt.plot(subset['Size'], subset['ExecutionTime'], marker='o', label=f'Version {version}')
plt.title('Average Execution Time')
plt.xlabel('Matrix Size')
plt.ylabel('Execution Time (seconds)')
plt.legend()
plt.grid()

# Gráfico do L1 DCM
plt.subplot(1, 3, 2)
for version in averages['Version'].unique():
    subset = averages[averages['Version'] == version]
    plt.plot(subset['Size'], subset['L1DCM'], marker='o', label=f'Version {version}')
plt.title('Average L1 DCM')
plt.xlabel('Matrix Size')
plt.ylabel('L1 DCM')
plt.legend()
plt.grid()

# Gráfico do L2 DCM
plt.subplot(1, 3, 3)
for version in averages['Version'].unique():
    subset = averages[averages['Version'] == version]
    plt.plot(subset['Size'], subset['L2DCM'], marker='o', label=f'Version {version}')
plt.title('Average L2 DCM')
plt.xlabel('Matrix Size')
plt.ylabel('L2 DCM')
plt.legend()
plt.grid()

# Exibir os gráficos
plt.tight_layout()
plt.show()