package buscatabu;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author thiago092
 */
public class Busca {

    double b;//Peso máximo
    private int maxBT = 3; //Número máximo de iterações sem melhora

    private String[] itemNames;//Nomes dos itens
    private double[] pesos;//Pesos dos itens
    private double[] lucro;//Valores dos itens

    private double alpha;//Somatório de todas as utilidades

    private int[] bestSolucao;
    private double bestAvaliacao;
    private int bestIt = 0; //Melhor iteração

    private int[] currentSolution;

    private List<Integer> tabu;

    public Busca() throws IOException {
        
        FileInputStream stream = new FileInputStream("C:\\Users\\aluno\\Downloads\\buscaTabu\\src\\arquivos\\f1_l-d_kp_10_269");
        InputStreamReader reader = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(reader);
        String linha = br.readLine();
        
        String arquivo_tamanho = linha.substring(linha.lastIndexOf(' '));
        Double tamanho = Double.parseDouble(arquivo_tamanho.replaceAll("\\.","").replace(",","."));
        
        //recebimento tamanho arquivo
        b =tamanho;
        
        
        // pegando quantidade de itens
        
        String qt_itens = linha.substring(0, linha.indexOf(' '));
        int qt_item = Integer.parseInt(qt_itens);
        
     
        //criando rotulos aos itens
        
        int i; // índice ou posição
        
         itemNames = new String[qt_item];
 
        // processando os "n" elementos do vetor "v"
        for (i=0; i<qt_item; i++) {
            
            String resultado = Integer.toString(i);
            itemNames[i] = resultado; // na i-ésima posição do vetor "v" armazena o valor da variável "i"
            }
        
        //inicialanzo lucro
        
        lucro = new double[qt_item];
        pesos = new double[qt_item];
        
        int vai = 0;
        i=0;
        
        while(linha != null) {
            vai++;
            String nome = linha.substring(0, linha.indexOf(' '));
            String cidade = linha.substring(linha.lastIndexOf(' ') + 1, linha.length());
            System.out.println(cidade);
            
            if(vai>1){
                i++;
               
                Double resultado = Double.parseDouble(nome.replaceAll("\\.","").replace(",","."));
                Double resultados = Double.parseDouble(cidade.replaceAll("\\.","").replace(",","."));
                lucro[i-1] = resultado;
                pesos[i-1] = resultados;
               System.out.println(Arrays.toString(pesos));
                
              }
            linha = br.readLine();
            
        }
        


       //fim
        
        //pesos = new double[]{95, 4, 60, 32, 23, 72, 80, 62, 65, 46};

       // lucro = new double[]{55,10,47,5,4,50,8,61,85,87};

        tabu = new ArrayList<>();

        currentSolution = new int[itemNames.length];
        bestSolucao = new int[itemNames.length];
        initAlpha();

        initFirstSolution();

        bestAvaliacao = avaliacao(currentSolution);
        bestSolucao = currentSolution.clone();
        bestIt = 0;
    }

    private void initAlpha() {
        for (double d : lucro) {
            alpha += d;
        }
    }

    private void initFirstSolution() {
        Random r = new Random();

        for (int i = 0; i < currentSolution.length; i++) {
            //currentSolution[i] = r.nextInt(2);
            currentSolution[i] = 1;
            
        }
    }

 
    /**
     * Método principal do algortimo
     */
    public void run() {
        int itAtual = 0;
        int random = 0;

        while ((itAtual - bestIt) < maxBT) {
            itAtual++;

            int[] bestNeighbor = findBestNeighbor(currentSolution);
            currentSolution = bestNeighbor.clone();
            double aval = avaliacao(bestNeighbor);

            if (aval > bestAvaliacao) {
                bestAvaliacao = aval;
                bestIt = itAtual;
                bestSolucao = bestNeighbor;
            }
        }
    }

    /**
     * Encontra o melhor vizinho da solução.
     *
     * @param currentSolution
     * @return
     */
    private int[] findBestNeighbor(int[] currentSolution) {
        int[][] neighbors = new int[currentSolution.length][currentSolution.length];

        for (int i = 0; i < currentSolution.length; i++) {
            int[] temp = currentSolution.clone();
            if (temp[i] == 0) {
                temp[i] = 1;
            } else {
                temp[i] = 0;
            }

            neighbors[i] = temp.clone();
        }

        int[] bestNeighborFound = new int[bestSolucao.length];
        double bestBeneficio = 0;
        int tabuPos = 0;

        for (int i = 0; i < neighbors.length; i++) {
            double val = avaliacao(neighbors[i]);
            if (val > bestBeneficio) {
                if (isTabu(i)) {
                    if (funcaoAspiracao(neighbors[i])) {
                        bestNeighborFound = neighbors[i].clone();
                        bestBeneficio = val;
                        tabuPos = i;
                    }
                } else {
                    bestNeighborFound = neighbors[i].clone();
                    bestBeneficio = val;
                    tabuPos = i;
                }
            }
        }
        addToTabu(tabuPos);
        return bestNeighborFound;
    }

    /**
     * Avalia se a solução gerada é melhor que a atual mesmo com as penalidades caso ela for maior
     * que o peso
     */
    private double avaliacao(int[] solution) {
        double beneficio = 0;
        for (int i = 0; i < solution.length; i++) {
            if (solution[i] == 1) {
                beneficio += lucro[i];
            }
        }

        double peso = 0;
        for (int i = 0; i < solution.length; i++) {
            if (solution[i] == 1) {
                peso += pesos[i];
            }
        }

        //System.out.println("Aval Beneficio: " + beneficio);
        //System.out.println("Aval peso: " + peso);
        double max = Math.max(0, peso - b);

        return beneficio - alpha * max;
    }

    /**
     * Gera o valor de uma posição aleatória do vetor de items
     *
     * @return
     */
    private int getRandomPosition() {
        return new Random().nextInt(itemNames.length);
    }

    /**
     * Adiciona para a lista tabu caso o tamanho dela for maior que 10
     *
     * @param value
     */
    private void addToTabu(int value) {
        if (tabu.size() > 15) {
            tabu.remove(0);
        }
        tabu.add(value);
    }

    private boolean isTabu(int i) {
        return tabu.contains(i);
    }

    private double calcBeneficio() {
        double value = 0;
        for (int i = 0; i < currentSolution.length; i++) {
            if (currentSolution[i] == 1) {
                value += lucro[i];
            }
        }
        return value;
    }

    /**
     * Função que avalia se deve ser permitido um movimento tabu após um tempo
     */
    private boolean funcaoAspiracao(int[] solution) {
        double beneficio = 0;

        for (int i = 0; i < solution.length; i++) {
            if (solution[i] == 1) {
                beneficio += lucro[i];
            }
        }

        double peso = 0;
        for (int i = 0; i < solution.length; i++) {
            if (solution[i] == 1) {
                peso += pesos[i];
            }
        }

        //System.out.println("Aval Beneficio: " + beneficio);
        //System.out.println("Aval peso: " + peso);
        double max = Math.max(0, peso - b);

        double aval = beneficio - alpha * max;

        return aval > bestAvaliacao;
    }

    public void printData() {
        System.out.println("\nItens: " + Arrays.toString(bestSolucao));
        System.out.println("Beneficio: " + bestAvaliacao);
        System.out.println("Melhor interação: " + bestIt);

        double beneficio = 0;
        double peso = 0;
        for (int i = 0; i < bestSolucao.length; i++) {
            if (bestSolucao[i] == 1) {
                System.out.println("Item: " + itemNames[i] + "\tPeso: " + pesos[i] + "\nBeneficio: " + lucro[i]);
                beneficio += lucro[i];
                peso += pesos[i];
            }
        }
        System.out.println("\nBeneficio real: " + beneficio);
        System.out.println("Peso real: " + peso);

    }
   

}
