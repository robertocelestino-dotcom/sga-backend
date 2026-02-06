package com.sga.aop;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.sga.service.SistemaLogService;

@Aspect
@Component
public class SistemaLogAspect {
    
    @Autowired
    private SistemaLogService sistemaLogService;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private ThreadLocal<Map<String, Object>> beforeState = new ThreadLocal<>();
    
    /**
     * Ponto de corte para métodos save() do JpaRepository
     */
    @Pointcut("execution(* org.springframework.data.jpa.repository.JpaRepository.save(..))")
    public void jpaSaveMethods() {}
    
    /**
     * Ponto de corte para métodos delete() do JpaRepository
     */
    @Pointcut("execution(* org.springframework.data.jpa.repository.JpaRepository.delete(..))")
    public void jpaDeleteMethods() {}
    
    /**
     * Ponto de corte para métodos customizados nos Services
     */
    @Pointcut("execution(* com.sga.service.*Service.*(..))")
    public void serviceMethods() {}
    
    /**
     * Ponto de corte para métodos nos Controllers
     */
    @Pointcut("execution(* com.sga.controller.*Controller.*(..))")
    public void controllerMethods() {}
    
    /**
     * Antes de salvar uma entidade
     */
    @Before("jpaSaveMethods() && args(entity)")
    public void beforeSave(Object entity) {
        try {
            // Obter o estado anterior se for uma atualização
            Object id = entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
            if (id != null) {
                Object entityManaged = entityManager.find(entity.getClass(), id);
                if (entityManaged != null) {
                    Map<String, Object> state = new HashMap<>();
                    state.put("entity", entityManaged);
                    state.put("isUpdate", true);
                    beforeState.set(state);
                } else {
                    Map<String, Object> state = new HashMap<>();
                    state.put("isUpdate", false);
                    beforeState.set(state);
                }
            } else {
                Map<String, Object> state = new HashMap<>();
                state.put("isUpdate", false);
                beforeState.set(state);
            }
        } catch (Exception e) {
            // Não quebra o fluxo principal se falhar
            beforeState.remove();
        }
    }
    
    /**
     * Após salvar com sucesso
     */
    @AfterReturning(value = "jpaSaveMethods()", returning = "result")
    public void afterSave(JoinPoint joinPoint, Object result) {
        try {
            Map<String, Object> state = beforeState.get();
            if (state != null) {
                boolean isUpdate = (boolean) state.getOrDefault("isUpdate", false);
                Object entityBefore = state.get("entity");
                
                String tabela = result.getClass().getSimpleName();
                Object id = entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(result);
                
                // Registrar após commit da transação
                if (TransactionSynchronizationManager.isSynchronizationActive()) {
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            sistemaLogService.registrarAcao(
                                tabela,
                                id != null ? Long.valueOf(id.toString()) : null,
                                isUpdate ? "UPDATE" : "CREATE",
                                getModuloFromEntity(result),
                                entityBefore,
                                result
                            );
                        }
                    });
                } else {
                    sistemaLogService.registrarAcao(
                        tabela,
                        id != null ? Long.valueOf(id.toString()) : null,
                        isUpdate ? "UPDATE" : "CREATE",
                        getModuloFromEntity(result),
                        entityBefore,
                        result
                    );
                }
            }
        } catch (Exception e) {
            // Não quebra o fluxo principal
            sistemaLogService.registrarErro("AOP", "afterSave", e, null);
        } finally {
            beforeState.remove();
        }
    }
    
    /**
     * Após deletar com sucesso
     */
    @AfterReturning("jpaDeleteMethods() && args(entity)")
    public void afterDelete(JoinPoint joinPoint, Object entity) {
        try {
            String tabela = entity.getClass().getSimpleName();
            Object id = entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
            
            sistemaLogService.registrarAcao(
                tabela,
                id != null ? Long.valueOf(id.toString()) : null,
                "DELETE",
                getModuloFromEntity(entity),
                entity, // Dados antes da exclusão
                null    // Dados depois (null porque foi deletado)
            );
        } catch (Exception e) {
            sistemaLogService.registrarErro("AOP", "afterDelete", e, null);
        }
    }
    
    /**
     * Após exceção em qualquer método de service
     */
    @AfterThrowing(pointcut = "serviceMethods()", throwing = "exception")
    public void afterServiceError(JoinPoint joinPoint, Exception exception) {
        try {
            String metodo = joinPoint.getSignature().toShortString();
            String modulo = joinPoint.getTarget().getClass().getSimpleName().replace("Service", "");
            
            sistemaLogService.registrarErro(modulo, metodo, exception, null);
        } catch (Exception e) {
            // Não faz nada se falhar
        }
    }
    
    /**
     * Método auxiliar para obter módulo a partir da entidade
     */
    private String getModuloFromEntity(Object entity) {
        String className = entity.getClass().getSimpleName();
        
        // Mapeamento de entidades para módulos
        Map<String, String> moduloMap = new HashMap<>();
        moduloMap.put("Associado", "ASSOCIADOS");
        moduloMap.put("Vendedor", "VENDEDORES");
        moduloMap.put("Produto", "PRODUTOS");
        moduloMap.put("Categoria", "CATEGORIAS");
        moduloMap.put("Planos", "PLANOS");
        moduloMap.put("Usuario", "USUARIOS");
        moduloMap.put("ImportacaoSPC", "IMPORTACAO");
        moduloMap.put("NotaDebitoSPC", "FINANCEIRO");
        moduloMap.put("Faturamento", "FATURAMENTO");
        moduloMap.put("Email", "CONTATOS");
        moduloMap.put("Telefone", "CONTATOS");
        moduloMap.put("Endereco", "CONTATOS");
        
        return moduloMap.getOrDefault(className, "OUTROS");
    }
}