import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Main {
    public static void main(String[] args) {
        try {
            // Создание построителя документа
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // Создание дерева DOM из документа
            Document document = documentBuilder.parse("Resources\\Balance.xml");
            // Получение списка всех элементов "Oper"
            NodeList operations = document.getElementsByTagName("Oper");
            // Инициализация переменной для хранения общей суммы списаний из xml-файла
            int totalCreditFromXml=0;
            // Инициализация переменной для хранения общей суммы зачислений из xml-файла
            int totalDebitFromXml=0;
            // Цикл по всем узлам "Oper"
            for (int i = 0; i < operations.getLength(); i++) {
                // Считывания статуса операции из дочернего узла "Status" для каждой операции
               String status=getTagValue("Status",(Element)operations.item(i));
               // Если статус операции-"Выполнена", то выполнить подсчёты сумм списаний и зачислений
                if (status.equals("Выполнена")) {
                    // Получение списка атрибутов каждого узла "Oper"
                    NamedNodeMap operationAttributes = operations.item(i).getAttributes();
                    // Если в атрибутах узла найден атрибут "cdt", суммировать списание к общей сумме списаний
                    if (operationAttributes.getNamedItem("cdt") != null) {
                        totalCreditFromXml += Integer.parseInt(operationAttributes.getNamedItem("cdt").getNodeValue());
                    }
                    /* Если в атрибутах узла атрибут "dbt" не пустой, суммировать начисление к общей сумме зачислений.
                    Такая обработка связана с дефектом выгружения xml-файла из программы тестового задания*/
                    if (!operationAttributes.getNamedItem("dbt").getNodeValue().equals("")) {
                        totalDebitFromXml += Integer.parseInt(operationAttributes.getNamedItem("dbt").getNodeValue());
                    }
                }
            }
            // Считывание файла лога и запись в коллекцию строк
            List <String> linesFromLog = Files.readAllLines(Paths.get("Resources\\testapp.log"), UTF_8);
            // Переменная для хранения входящего остатка, значение для которой берётся из лога
            int startRestFromLog=getValueFromLog(linesFromLog,4);
            // Получение списка всех элементов "Ballance"
            NodeList balance = document.getElementsByTagName("Ballance");
            // Переменная для хранения входящего остатка, значение для которой берётся из xml-файла.
            int startRestFromXml=Integer.parseInt(getTagValue("StartRest",(Element) balance.item(0)));
            // Переменная для хранения общей суммы списаний, значение для которой берётся из лога
            int totalCreditFromLog=getValueFromLog(linesFromLog,3);
            // Переменная для хранения общей суммы зачислений, значение для которой берётся из лога
            int totalDebitFromLog=getValueFromLog(linesFromLog,2);
            // Переменная для хранения исходящего остатка, значение для которой берётся из лога
            int restFromLog=getValueFromLog(linesFromLog,1);
            // Получение списка атрибутов узла "Ballance"
            NamedNodeMap balanceAttributes = balance.item(0).getAttributes();
            // Переменная для хранения исходящего остатка, значение для которой берётся из xml-файла
            int restFromXml=Integer.parseInt(balanceAttributes.getNamedItem("Rest").getNodeValue());
            // Напечатать результаты расчетов
            printResults(totalCreditFromXml,totalDebitFromXml,startRestFromXml,restFromXml,totalCreditFromLog,totalDebitFromLog,startRestFromLog,restFromXml);
        }
        catch (ParserConfigurationException | SAXException | IOException exception) {
            exception.printStackTrace(System.out);
        }
    }
    // Метод получения значения указанного тэга по названию тэга и узлу родителя
    private static String getTagValue(String tag, Element element) {
        try {
            NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
            return nodeList.item(0).getNodeValue();
        }
        catch (NullPointerException exception) {
            exception.printStackTrace(System.out);
            return "";
        }
    }
    // Метод получения нужного значения из лога
    private static int getValueFromLog(List <String> log,int lineIndex){
        try {
            // Нужное значение всегда находится в конце строки перед последним пробелом
            return Integer.parseInt(log.get(log.size() - lineIndex).substring(log.get(log.size() - lineIndex).lastIndexOf(' ') + 1));
        }
        catch (StringIndexOutOfBoundsException exception) {
            exception.printStackTrace(System.out);
            return 0;
        }
    }
    // Метод вывода результатов работы программы
    private static void printResults(int totalCreditFromXml,int totalDebitFromXml,int startRestFromXml,int restFromXml,int totalCreditFromLog,int totalDebitFromLog,int startRestFromLog,int restFromLog)
    {
        System.out.println("Данные из log-файла");
        System.out.println("-----------------------------------------------------");
        System.out.println("Входящий остаток по счёту = "+startRestFromLog);
        System.out.println("Обороты по списанию = "+totalCreditFromLog);
        System.out.println("Обороты по зачислению = "+totalDebitFromLog);
        System.out.println("Исходящий остаток по счёту, указанный в логе = "+restFromLog);
        // Переменная для расчета исходящего остатка по счету из данных лога
        int calculatedRestFromLog=startRestFromLog+totalDebitFromLog-totalCreditFromLog;
        System.out.println("Исходящий остаток по счёту, рассчитанный по данным в логе = "+calculatedRestFromLog+"\n");
        System.out.println("Данные, рассчитанные по значениям из xml-файла");
        System.out.println("-----------------------------------------------------");
        System.out.println("Входящий остаток по счёту = "+startRestFromXml);
        System.out.println("Обороты по списанию = "+totalCreditFromXml);
        System.out.println("Обороты по зачислению = "+totalDebitFromXml);
        System.out.println("Исходящий остаток по счёту, указанный в xml-файле = "+restFromXml);
        // Переменная для расчета исходящего остатка по счету, рассчитываемая по данным в xml-файле
        int calculatedRestFromXml=startRestFromXml+totalDebitFromXml-totalCreditFromXml;
        System.out.println("Исходящий остаток по счёту, рассчитанный по данным в xml-файле = "+calculatedRestFromXml+"\n");
        System.out.println("Итого:");
        System.out.println("-----------------------------------------------------");
        System.out.println("Входящие остатки по счёту в логе и в xml-файле "+ resultOfComparison(startRestFromLog,startRestFromXml));
        System.out.println("Обороты по списанию в логе и в xml-файле "+ resultOfComparison(totalCreditFromLog,totalCreditFromXml));
        System.out.println("Обороты по зачислению в логе и в xml-файле "+ resultOfComparison(totalDebitFromLog,totalDebitFromXml));
        System.out.println("Исходящие остатки по счёту, указанные в логе и в xml-файле "+ resultOfComparison(restFromLog,restFromXml));
        System.out.println("Исходящие остатки по счёту, рассчитанные по данным в логе и по данным в xml-файле "+ resultOfComparison(calculatedRestFromLog,calculatedRestFromXml));
    }
    // Метод для возврата результата сравнения двух чисел
    private static String resultOfComparison(int x1, int x2) {
        if (x1==x2) return "равны";
        else return "отличаются";
    }
}